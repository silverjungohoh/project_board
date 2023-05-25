package com.study.boardserver.domain.member.service;

import com.study.boardserver.domain.mail.service.MailService;
import com.study.boardserver.domain.member.dto.signup.ConfirmAuthCodeRequest;
import com.study.boardserver.domain.member.dto.signup.SignUpRequest;
import com.study.boardserver.domain.member.dto.signup.SignUpResponse;
import com.study.boardserver.domain.member.entity.Member;
import com.study.boardserver.domain.member.entity.MemberAuthCode;
import com.study.boardserver.domain.member.repository.MemberRepository;
import com.study.boardserver.domain.member.repository.redis.MemberAuthCodeRepository;
import com.study.boardserver.domain.member.type.MemberRole;
import com.study.boardserver.domain.member.type.MemberStatus;
import com.study.boardserver.domain.security.oauth2.type.ProviderType;
import com.study.boardserver.global.error.exception.MemberException;
import com.study.boardserver.global.error.type.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MailService mailService;
    private final MemberAuthCodeRepository memberAuthCodeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Map<String, String> checkDuplicatedEmail(String email) {
        if(memberRepository.existsByEmail(email)) {
            throw new MemberException(MemberErrorCode.DUPLICATED_EMAIL);
        }
        return getMessage("사용 가능한 이메일입니다.");
    }

    @Override
    public Map<String, String> checkDuplicatedNickname(String nickname) {
        if(memberRepository.existsByNickname(nickname)) {
            throw new MemberException(MemberErrorCode.DUPLICATED_NICKNAME);
        }
        return getMessage("사용 가능한 닉네임입니다.");
    }

    @Override
    public Map<String, String> sendAuthCode(String email) {
        if(memberRepository.existsByEmail(email)) {
            throw new MemberException(MemberErrorCode.DUPLICATED_EMAIL);
        }

        String authCode = UUID.randomUUID().toString().substring(0, 8);
        mailService.sendMail(email, authCode);

        MemberAuthCode code = MemberAuthCode.builder()
                .id(authCode)
                .expiredAt(180L)
                .email(email)
                .build();

        memberAuthCodeRepository.save(code);

        return getMessage("이메일 인증 코드를 전송하였습니다.");
    }

    @Override
    public Map<String, String> confirmAuthCode(ConfirmAuthCodeRequest request) {

        memberAuthCodeRepository.findByIdAndEmail(request.getCode(), request.getEmail())
                .orElseThrow(()-> new MemberException(MemberErrorCode.INVALID_EMAIL_AUTH_CODE));

        return getMessage("이메일 인증이 완료되었습니다.");
    }

    @Override
    public SignUpResponse signUp(SignUpRequest request) {

        if(memberRepository.existsByEmail(request.getEmail())) {
            throw new MemberException(MemberErrorCode.DUPLICATED_EMAIL);
        }

        if(memberRepository.existsByNickname(request.getNickname())) {
            throw new MemberException(MemberErrorCode.DUPLICATED_NICKNAME);
        }

        if(!request.isEmailAuth()) {
            throw new MemberException(MemberErrorCode.NOT_FINISH_EMAIL_AUTH);
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .name(request.getName())
                .birth(request.getBirth())
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.ROLE_USER)
                .providerType(ProviderType.LOCAL)
                .build();

        memberRepository.save(member);

        return SignUpResponse.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .build();
    }


    private static Map<String, String> getMessage(String message) {
        Map<String, String> result = new HashMap<>();
        result.put("message", message);
        return result;
    }
}
