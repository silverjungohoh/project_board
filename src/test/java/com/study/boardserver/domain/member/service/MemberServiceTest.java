package com.study.boardserver.domain.member.service;

import com.study.boardserver.domain.mail.service.MailService;
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
import com.study.boardserver.domain.security.CustomUserDetails;
import com.study.boardserver.domain.security.jwt.JwtTokenProvider;
import com.study.boardserver.domain.security.jwt.redis.LogoutAccessToken;
import com.study.boardserver.domain.security.jwt.redis.LogoutAccessTokenRepository;
import com.study.boardserver.domain.security.jwt.redis.RefreshToken;
import com.study.boardserver.domain.security.jwt.redis.RefreshTokenRepository;
import com.study.boardserver.global.error.exception.MemberAuthException;
import com.study.boardserver.global.error.exception.MemberException;
import com.study.boardserver.global.error.type.MemberAuthErrorCode;
import com.study.boardserver.global.error.type.MemberErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MailService mailService;

    @Mock
    private MemberAuthCodeRepository memberAuthCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private LogoutAccessTokenRepository logoutAccessTokenRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    @DisplayName("이메일 중복 확인 - 중복 없음")
    void checkDuplicatedEmail_No() {
        String email = "test@test.com";
        // given
        given(memberRepository.existsByEmail(anyString())).willReturn(false);
        // when
        Map<String, String> result = memberService.checkDuplicatedEmail(email);
        // then
        assertNotNull(result.get("message"));
    }

    @Test
    @DisplayName("이메일 중복 확인 - 중복 있음")
    void checkDuplicatedEmail_Yes() {
        String email = "test@test.com";
        // given
        given(memberRepository.existsByEmail(anyString())).willReturn(true);
        // when
        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.checkDuplicatedEmail(email));
        // then
        assertEquals(exception.getErrorCode(), MemberErrorCode.DUPLICATED_EMAIL);
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 중복 없음")
    void checkDuplicatedNickname_No() {
        String nick = "nick";
        // given
        given(memberRepository.existsByNickname(anyString())).willReturn(false);
        // when
        Map<String, String> result = memberService.checkDuplicatedNickname(nick);
        // then
        assertNotNull(result.get("message"));
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 중복 있음")
    void checkDuplicatedNickname_Yes() {
        String nick = "nick";
        // given
        given(memberRepository.existsByNickname(anyString())).willReturn(true);

        // when
        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.checkDuplicatedNickname(nick));
        // then
        assertEquals(exception.getErrorCode(), MemberErrorCode.DUPLICATED_NICKNAME);
    }

    @Test
    @DisplayName("이메일 인증 코드 발송 실패")
    void sendAuthCode_Fail() {
        String email = "test@test.com";
        // given
        given(memberRepository.existsByEmail(anyString())).willReturn(true);
        // when
        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.sendAuthCode(email));
        // then
        assertEquals(exception.getErrorCode(), MemberErrorCode.DUPLICATED_EMAIL);
    }

    @Test
    @DisplayName("이메일 인증 코드 발송 성공")
    void sendAuthCode_Success() {
        String email = "test@test.com";

        // given
        given(memberRepository.existsByEmail(anyString())).willReturn(false);
        given(mailService.sendMail(anyString(), anyString())).willReturn(true);

        ArgumentCaptor<MemberAuthCode> captor = ArgumentCaptor.forClass(MemberAuthCode.class);

        // when
        Map<String, String> result = memberService.sendAuthCode(email);

        // then
        assertNotNull(result.get("message"));
        verify(memberAuthCodeRepository, times(1)).save(captor.capture());
    }

    @Test
    @DisplayName("회원 이메일 인증 실패")
    void confirmAuthCode_Fail() {
        ConfirmAuthCodeRequest request = ConfirmAuthCodeRequest.builder()
                .email("test@test.com")
                .code("abc123")
                .build();

        // given
        given(memberAuthCodeRepository.findByIdAndEmail(anyString(), anyString())).willReturn(Optional.empty());

        // when
        MemberException exception = assertThrows(MemberException.class,
                ()-> memberService.confirmAuthCode(request));

        // then
        assertEquals(exception.getErrorCode(), MemberErrorCode.INVALID_EMAIL_AUTH_CODE);
    }

    @Test
    @DisplayName("회원 이메일 인증 성공")
    void confirmAuthCode_Success() {
        ConfirmAuthCodeRequest request = ConfirmAuthCodeRequest.builder()
                .email("test@test.com")
                .code("abc123")
                .build();

        MemberAuthCode authCode = MemberAuthCode.builder()
                .email(request.getEmail())
                .id(request.getCode())
                .expiredAt(100L)
                .build();

        // given
        given(memberAuthCodeRepository.findByIdAndEmail(anyString(), anyString())).willReturn(Optional.of(authCode));

        // when
        Map<String, String> result = memberService.confirmAuthCode(request);

        // then
        assertNotNull(result.get("message"));
    }

    @Test
    @DisplayName("회원 가입 실패 - 이메일 인증 X")
    void signUp_NoAuth_Fail() {
        SignUpRequest request = SignUpRequest.builder()
                .email("test@test.com")
                .nickname("광어우럭")
                .name("이름")
                .password("test1234!!")
                .emailAuth(false)
                .birth(LocalDate.of(2000, 1, 1))
                .build();

        MemberException exception = assertThrows(MemberException.class,
                ()-> memberService.signUp(request));

        assertEquals(exception.getErrorCode(), MemberErrorCode.NOT_FINISH_EMAIL_AUTH);
    }
    @Test
    @DisplayName("회원 가입 실패 - 이메일 중복")
    void signUp_DuplicatedEmail_Fail() {
        SignUpRequest request = SignUpRequest.builder()
                .email("test@test.com")
                .nickname("광어우럭")
                .name("이름")
                .password("test1234!!")
                .birth(LocalDate.of(2000, 1, 1))
                .build();

        given(memberRepository.existsByEmail(anyString())).willReturn(true);

        MemberException exception = assertThrows(MemberException.class,
                ()-> memberService.signUp(request));

        assertEquals(exception.getErrorCode(), MemberErrorCode.DUPLICATED_EMAIL);
    }

    @Test
    @DisplayName("회원 가입 실패 - 닉네임 중복")
    void signUp_DuplicatedNickname_Fail() {
        SignUpRequest request = SignUpRequest.builder()
                .email("test@test.com")
                .nickname("광어우럭")
                .name("이름")
                .password("test1234!!")
                .birth(LocalDate.of(2000, 1, 1))
                .build();

        given(memberRepository.existsByEmail(anyString())).willReturn(false);
        given(memberRepository.existsByNickname(anyString())).willReturn(true);

        MemberException exception = assertThrows(MemberException.class,
                ()-> memberService.signUp(request));

        assertEquals(exception.getErrorCode(), MemberErrorCode.DUPLICATED_NICKNAME);
    }


    @Test
    @DisplayName("회원 가입 성공")
    void signUp_Success() {
        SignUpRequest request = SignUpRequest.builder()
                .email("test@test.com")
                .nickname("광어우럭")
                .name("이름")
                .password("test1234!!")
                .emailAuth(true)
                .birth(LocalDate.of(2000, 1, 1))
                .build();

        given(memberRepository.existsByEmail(anyString())).willReturn(false);
        given(memberRepository.existsByNickname(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("password1!");

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

        SignUpResponse response = memberService.signUp(request);

        assertEquals(response.getEmail(), request.getEmail());
        assertEquals(response.getNickname(), request.getNickname());
        verify(memberRepository, times(1)).save(captor.capture());
    }

    @Test
    @DisplayName("access token 재발급 실패 - 토큰 유효 X")
    void reissueToken_Fail_Invalid() {
        ReissueTokenRequest request = ReissueTokenRequest.builder()
                .refreshToken("refresh-token")
                .build();

        given(jwtTokenProvider.validateToken(anyString())).willReturn(false);

        MemberAuthException exception = assertThrows(MemberAuthException.class,
                ()-> memberService.reissueToken(request));

        assertEquals(exception.getErrorCode(), MemberAuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("access token 재발급 실패 - 토큰 존재 X")
    void reissueToken_Fail_NotExist() {
        ReissueTokenRequest request = ReissueTokenRequest.builder()
                .refreshToken("refresh-token")
                .build();

        String email = "test@test.com";
        String role = "ROLE_USER";

        given(jwtTokenProvider.validateToken(request.getRefreshToken())).willReturn(true);
        given(jwtTokenProvider.getUsername(anyString())).willReturn(email);
        given(jwtTokenProvider.getUserRole(anyString())).willReturn(role);
        given(refreshTokenRepository.findById(anyString())).willReturn(Optional.empty());

        MemberAuthException exception = assertThrows(MemberAuthException.class,
                ()-> memberService.reissueToken(request));

        assertEquals(exception.getErrorCode(), MemberAuthErrorCode.NOT_EXIST_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("access token 재발급 실패 - 일치 X")
    void reissueToken_Fail_NotMatch() {
        ReissueTokenRequest request = ReissueTokenRequest.builder()
                .refreshToken("refresh-token1")
                .build();

        String email = "test@test.com";
        String role = "ROLE_USER";

        RefreshToken refreshToken = RefreshToken.builder()
                .id(email)
                .refreshToken("refresh-token2")
                .build();

        given(jwtTokenProvider.validateToken(request.getRefreshToken())).willReturn(true);
        given(jwtTokenProvider.getUsername(anyString())).willReturn(email);
        given(jwtTokenProvider.getUserRole(anyString())).willReturn(role);
        given(refreshTokenRepository.findById(anyString())).willReturn(Optional.of(refreshToken));

        MemberAuthException exception = assertThrows(MemberAuthException.class,
                () -> memberService.reissueToken(request));

        assertEquals(exception.getErrorCode(), MemberAuthErrorCode.NOT_MATCH_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("access token 재발급 성공")
    void reissueToken_Success() {
        ReissueTokenRequest request = ReissueTokenRequest.builder()
                .refreshToken("refresh-token")
                .build();

        String email = "test@test.com";
        String role = "ROLE_USER";

        RefreshToken refreshToken = RefreshToken.builder()
                .id(email)
                .refreshToken("refresh-token")
                .build();

        String accessToken = "access-token";

        given(jwtTokenProvider.validateToken(request.getRefreshToken())).willReturn(true);
        given(jwtTokenProvider.getUsername(anyString())).willReturn(email);
        given(jwtTokenProvider.getUserRole(anyString())).willReturn(role);
        given(refreshTokenRepository.findById(anyString())).willReturn(Optional.of(refreshToken));
        given(jwtTokenProvider.issueAccessToken(anyString(), anyString())).willReturn(accessToken);

        ReissueTokenResponse response = memberService.reissueToken(request);

        assertEquals(response.getAccessToken(), accessToken);
    }

    @Test
    @DisplayName("로그아웃 실패")
    void logout_Fail() {
        LogoutRequest request = LogoutRequest.builder()
                .accessToken("access-token")
                .build();

        MemberAuthException exception = assertThrows(MemberAuthException.class,
                () -> memberService.logout(request));

        assertEquals(exception.getErrorCode(), MemberAuthErrorCode.INVALID_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        LogoutRequest request = LogoutRequest.builder()
                .accessToken("access-token")
                .build();

        Member member = Member.builder()
                .email("test@test.com")
                .nickname("닉네임")
                .password("password123!")
                .role(MemberRole.ROLE_USER)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

        given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
        given(jwtTokenProvider.getAuthentication(anyString())).willReturn(authentication);
        given(jwtTokenProvider.getRemainingTime(anyString())).willReturn(10000L);

        ArgumentCaptor<LogoutAccessToken> captor = ArgumentCaptor.forClass(LogoutAccessToken.class);

        Map<String, String> result = memberService.logout(request);

        assertNotNull(result.get("message"));
        verify(logoutAccessTokenRepository, times(1)).save(captor.capture());
    }
}