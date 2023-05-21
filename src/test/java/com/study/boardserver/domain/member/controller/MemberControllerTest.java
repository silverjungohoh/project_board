package com.study.boardserver.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.boardserver.domain.member.service.MemberService;
import com.study.boardserver.global.error.exception.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static com.study.boardserver.global.error.type.MemberErrorCode.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("이메일 중복 확인 시 중복 없음")
    void checkDuplicatedEmail_No() throws Exception {

        Map<String, String> request = new HashMap<>();
        request.put("email", "test@test.com");

        Map<String, String> response = new HashMap<>();
        response.put("message", "사용 가능한 이메일입니다.");

        given(memberService.checkDuplicatedEmail(anyString())).willReturn(response);

        mockMvc.perform(post("/api/members/sign-up/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(response.get("message")))
                .andDo(print());

        verify(memberService).checkDuplicatedEmail(request.get("email"));
    }

    @Test
    @DisplayName("이메일 중복 확인 시 중복 있음")
    void checkDuplicatedEmail_Yes() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@test.com");

        given(memberService.checkDuplicatedEmail(anyString()))
                .willThrow(new MemberException(DUPLICATED_EMAIL));

        mockMvc.perform(post("/api/members/sign-up/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(DUPLICATED_EMAIL.getStatus().value()))
                .andExpect(jsonPath("$.message").value(DUPLICATED_EMAIL.getMessage()))
                .andDo(print());

        verify(memberService).checkDuplicatedEmail(request.get("email"));
    }

    @Test
    @DisplayName("닉네임 중복 확인 시 중복 없음")
    void checkDuplicatedNickname_No() throws Exception {

        Map<String, String> request = new HashMap<>();
        request.put("nickname", "광어광어우럭");

        Map<String, String> response = new HashMap<>();
        response.put("message", "사용 가능한 닉네임입니다.");

        given(memberService.checkDuplicatedNickname(anyString())).willReturn(response);

        mockMvc.perform(post("/api/members/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(response.get("message")))
                .andDo(print());

        verify(memberService).checkDuplicatedNickname(request.get("nickname"));
    }

    @Test
    @DisplayName("닉네임 중복 확인 시 중복 있음")
    void checkDuplicatedNickname_Yes() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("nickname", "광어광어우럭");

        given(memberService.checkDuplicatedNickname(anyString()))
                .willThrow(new MemberException(DUPLICATED_NICKNAME));

        mockMvc.perform(post("/api/members/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(DUPLICATED_NICKNAME.getStatus().value()))
                .andExpect(jsonPath("$.message").value(DUPLICATED_NICKNAME.getMessage()))
                .andDo(print());

        verify(memberService).checkDuplicatedNickname(request.get("nickname"));
    }

    @Test
    @DisplayName("이메일 인증 코드 전송 성공")
    void sendAuthCode_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@test.com");

        Map<String, String> response = new HashMap<>();
        response.put("message", "이메일 인증 코드를 전송하였습니다.");

        given(memberService.sendAuthCode(anyString())).willReturn(response);

        mockMvc.perform(post("/api/members/email-auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(response.get("message")))
                .andDo(print());

        verify(memberService).sendAuthCode(request.get("email"));
    }

    @Test
    @DisplayName("이메일 인증 코드 전송 실패 - 이메일 중복")
    void sendAuthCode_Fail_DuplicatedEmail() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@test.com");

        given(memberService.sendAuthCode(anyString())).willThrow(new MemberException(DUPLICATED_EMAIL));

        mockMvc.perform(post("/api/members/email-auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(DUPLICATED_EMAIL.getStatus().value()))
                .andExpect(jsonPath("$.message").value(DUPLICATED_EMAIL.getMessage()))
                .andDo(print());

        verify(memberService).sendAuthCode(request.get("email"));
    }

    @Test
    @DisplayName("이메일 인증 코드 전송 실패")
    void sendAuthCode_Fail() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@test.com");

        given(memberService.sendAuthCode(anyString())).willThrow(new MemberException(FAIL_TO_SEND_EMAIL));

        mockMvc.perform(post("/api/members/email-auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(FAIL_TO_SEND_EMAIL.getStatus().value()))
                .andExpect(jsonPath("$.message").value(FAIL_TO_SEND_EMAIL.getMessage()))
                .andDo(print());

        verify(memberService).sendAuthCode(request.get("email"));
    }

    @Test
    @DisplayName("회원 이메일 인증 성공")
    void confirmAuthCode_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("code", "abc123");

        Map<String, String> response = new HashMap<>();
        response.put("message", "이메일 인증이 완료되었습니다.");

        given(memberService.confirmAuthCode(anyString())).willReturn(response);

        mockMvc.perform(post("/api/members/email-authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(response.get("message")))
                .andDo(print());

    }

    @Test
    @DisplayName("회원 이메일 인증 실패")
    void confirmAuthCode_Fail() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("code", "abc123");

        given(memberService.confirmAuthCode(anyString())).willThrow(new MemberException(INVALID_EMAIL_AUTH_CODE));

        mockMvc.perform(post("/api/members/email-authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(INVALID_EMAIL_AUTH_CODE.getStatus().value()))
                .andExpect(jsonPath("$.message").value(INVALID_EMAIL_AUTH_CODE.getMessage()))
                .andDo(print());
    }
}