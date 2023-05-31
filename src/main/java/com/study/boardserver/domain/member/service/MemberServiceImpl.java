package com.study.boardserver.domain.member.service;

import com.study.boardserver.domain.mail.service.MailService;
import com.study.boardserver.domain.member.dto.login.LoginRequest;
import com.study.boardserver.domain.member.dto.login.LoginResponse;
import com.study.boardserver.domain.member.dto.logout.LogoutRequest;
import com.study.boardserver.domain.member.dto.reissue.ReissueTokenRequest;
import com.study.boardserver.domain.member.dto.reissue.ReissueTokenResponse;
import com.study.boardserver.domain.member.dto.signup.ConfirmAuthCodeRequest;
import com.study.boardserver.domain.member.dto.signup.SignUpRequest;
import com.study.boardserver.domain.member.dto.signup.SignUpResponse;
import com.study.boardserver.domain.member.entity.Member;
import com.study.boardserver.domain.member.entity.MemberAuthCode;
import com.study.boardserver.domain.member.repository.MemberRepository;
import com.study.boardserver.domain.member.repository.redis.MemberAuthCodeRepository;
import com.study.boardserver.domain.member.type.MemberRole;
import com.study.boardserver.domain.member.type.MemberStatus;
import com.study.boardserver.domain.security.CustomUserDetails;
import com.study.boardserver.domain.security.jwt.JwtTokenProvider;
import com.study.boardserver.domain.security.jwt.redis.LogoutAccessToken;
import com.study.boardserver.domain.security.jwt.redis.LogoutAccessTokenRepository;
import com.study.boardserver.domain.security.jwt.redis.RefreshToken;
import com.study.boardserver.domain.security.jwt.redis.RefreshTokenRepository;
import com.study.boardserver.domain.security.oauth2.type.ProviderType;
import com.study.boardserver.global.error.exception.MemberAuthException;
import com.study.boardserver.global.error.exception.MemberException;
import com.study.boardserver.global.error.type.MemberAuthErrorCode;
import com.study.boardserver.global.error.type.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LogoutAccessTokenRepository logoutAccessTokenRepository;

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

    @Override
    public LoginResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.issueAccessToken(userDetails.getUsername(), userDetails.getRole().name());
        String refreshToken = jwtTokenProvider.issueRefreshToken(userDetails.getUsername(), userDetails.getRole().name());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public ReissueTokenResponse reissueToken(ReissueTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new MemberAuthException(MemberAuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        String email = jwtTokenProvider.getUsername(refreshToken);
        String role = jwtTokenProvider.getUserRole(refreshToken);

        RefreshToken findRefreshToken  = refreshTokenRepository.findById(email)
                .orElseThrow(() -> new MemberAuthException(MemberAuthErrorCode.NOT_EXIST_REFRESH_TOKEN));

        if(!refreshToken.equals(findRefreshToken.getRefreshToken())) {
            throw new MemberAuthException(MemberAuthErrorCode.NOT_MATCH_REFRESH_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.issueAccessToken(email, role);

        return ReissueTokenResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    @Override
    public Map<String, String> logout(LogoutRequest request) {
        String accessToken = request.getAccessToken();

        try {
            jwtTokenProvider.validateAccessToken(accessToken);
        } catch (MemberAuthException e) {
            throw new MemberAuthException(MemberAuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        String email = authentication.getName();

        refreshTokenRepository.deleteById(email);

        LogoutAccessToken logoutAccessToken = LogoutAccessToken.builder()
                .id(accessToken)
                .expiration(jwtTokenProvider.getRemainingTime(accessToken))
                .build();

        logoutAccessTokenRepository.save(logoutAccessToken);

        return getMessage("로그아웃");
    }

    private static Map<String, String> getMessage(String message) {
        Map<String, String> result = new HashMap<>();
        result.put("message", message);
        return result;
    }
}
