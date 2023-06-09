package com.study.boardserver.domain.security.service;

import com.study.boardserver.domain.member.entity.Member;
import com.study.boardserver.domain.member.repository.MemberRepository;
import com.study.boardserver.domain.member.type.MemberRole;
import com.study.boardserver.domain.member.type.MemberStatus;
import com.study.boardserver.domain.security.CustomUserDetails;
import com.study.boardserver.domain.security.oauth2.info.OAuth2UserInfo;
import com.study.boardserver.domain.security.oauth2.info.OAuth2UserInfoFactory;
import com.study.boardserver.domain.security.oauth2.type.ProviderType;
import com.study.boardserver.global.error.exception.MemberAuthException;
import com.study.boardserver.global.error.type.MemberAuthErrorCode;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String PASSWORD = "password";

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return process(userRequest, oAuth2User);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new InternalAuthenticationServiceException(e.getMessage(), e.getCause());
        }
    }

    private OAuth2User process(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {

        ProviderType providerType
                = ProviderType.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, oAuth2User.getAttributes());

        Optional<Member> savedMember = memberRepository.findByEmail(userInfo.getEmail());

        Member member;
        if (savedMember.isPresent()) {
            member = savedMember.get();

            if (!Objects.equals(member.getProviderType(), providerType)) {
                throw new MemberAuthException(MemberAuthErrorCode.NOT_MATCH_PROVIDER_TYPE);
            }
            update(userInfo, member);
        } else {
            member = signUp(userInfo, providerType);
        }

        return new CustomUserDetails(member, oAuth2User.getAttributes());
    }


    /**
     * 기존 회원이 아닌 경우 회원 가입 처리
     */
    private Member signUp(OAuth2UserInfo userInfo, ProviderType providerType) {
        String uuid = UUID.randomUUID().toString().substring(0, 18);

        Member newMember = Member.builder()
                .email(userInfo.getEmail())
                .password(BCrypt.hashpw(PASSWORD + uuid, BCrypt.gensalt()))
                .nickname(providerType + "_" + uuid)
                .role(MemberRole.ROLE_USER)
                .imgUrl(userInfo.getImageUrl())
                .status(MemberStatus.ACTIVE)
                .providerType(providerType)
                .build();

        memberRepository.saveAndFlush(newMember);

        return newMember;
    }

    /**
     * 기존 회원인 경우 회원 정보 업데이트
     */
    private void update(OAuth2UserInfo userInfo, Member member) {

        if (!Objects.equals(member.getImgUrl(), userInfo.getImageUrl())) {
            member.setImgUrl(userInfo.getImageUrl());
        }
        memberRepository.save(member);
    }
}
