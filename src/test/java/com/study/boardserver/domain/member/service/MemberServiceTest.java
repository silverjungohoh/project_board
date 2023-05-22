package com.study.boardserver.domain.member.service;

import com.study.boardserver.domain.mail.service.MailService;
import com.study.boardserver.domain.member.dto.signup.ConfirmAuthCodeRequest;
import com.study.boardserver.domain.member.dto.signup.SignUpRequest;
import com.study.boardserver.domain.member.dto.signup.SignUpResponse;
import com.study.boardserver.domain.member.entity.Member;
import com.study.boardserver.domain.member.entity.MemberAuthCode;
import com.study.boardserver.domain.member.repository.MemberRepository;
import com.study.boardserver.domain.member.repository.redis.MemberAuthCodeRepository;
import com.study.boardserver.global.error.exception.MemberException;
import com.study.boardserver.global.error.type.MemberErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    @DisplayName("이메일 중복 확인 - 중복 없음")
    void checkDuplicatedEmail_No() {
        String email = "test@test.com";
        // given
        given(memberRepository.findByEmail(anyString())).willReturn(Optional.empty());
        // when
        Map<String, String> result = memberService.checkDuplicatedEmail(email);
        // then
        assertNotNull(result.get("message"));
    }

    @Test
    @DisplayName("이메일 중복 확인 - 중복 있음")
    void checkDuplicatedEmail_Yes() {
        String email = "test@test.com";
        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("nick")
                .build();

        // given
        given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(member));
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
        given(memberRepository.findByNickname(anyString())).willReturn(Optional.empty());
        // when
        Map<String, String> result = memberService.checkDuplicatedNickname(nick);
        // then
        assertNotNull(result.get("message"));
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 중복 있음")
    void checkDuplicatedNickname_Yes() {
        String nick = "nick";
        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("nick")
                .build();

        // given
        given(memberRepository.findByNickname(anyString())).willReturn(Optional.of(member));

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
        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("nick")
                .build();

        // given
        given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(member));
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
        given(memberRepository.findByEmail(anyString())).willReturn(Optional.empty());
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
}