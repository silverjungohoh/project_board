package com.study.boardserver.domain.member.service;

import com.study.boardserver.domain.mail.service.MailService;
import com.study.boardserver.domain.member.entity.Member;
import com.study.boardserver.domain.member.repository.MemberRepository;
import com.study.boardserver.global.error.exception.MemberException;
import com.study.boardserver.global.error.type.MemberErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MailService mailService;

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

        // when
        Map<String, String> result = memberService.sendAuthCode(email);

        // then
        assertNotNull(result.get("message"));
    }
}