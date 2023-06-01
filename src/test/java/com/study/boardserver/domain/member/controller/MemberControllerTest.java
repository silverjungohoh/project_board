package com.study.boardserver.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.boardserver.domain.member.dto.login.LoginRequest;
import com.study.boardserver.domain.member.dto.login.LoginResponse;
import com.study.boardserver.domain.member.dto.logout.LogoutRequest;
import com.study.boardserver.domain.member.dto.reissue.ReissueTokenRequest;
import com.study.boardserver.domain.member.dto.reissue.ReissueTokenResponse;
import com.study.boardserver.domain.member.dto.signup.ConfirmAuthCodeRequest;
import com.study.boardserver.domain.member.dto.signup.SignUpRequest;
import com.study.boardserver.domain.member.dto.signup.SignUpResponse;
import com.study.boardserver.domain.member.service.MemberService;
import com.study.boardserver.global.error.exception.MemberAuthException;
import com.study.boardserver.global.error.exception.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static com.study.boardserver.global.error.type.MemberAuthErrorCode.*;
import static com.study.boardserver.global.error.type.MemberErrorCode.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
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
        ConfirmAuthCodeRequest request = ConfirmAuthCodeRequest.builder()
                .email("test@test.com")
                .code("abc123")
                .build();

        Map<String, String> response = new HashMap<>();
        response.put("message", "이메일 인증이 완료되었습니다.");

        given(memberService.confirmAuthCode(any())).willReturn(response);

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
        ConfirmAuthCodeRequest request = ConfirmAuthCodeRequest.builder()
                .email("test@test.com")
                .code("abc123")
                .build();

        given(memberService.confirmAuthCode(any())).willThrow(new MemberException(INVALID_EMAIL_AUTH_CODE));

        mockMvc.perform(post("/api/members/email-authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(INVALID_EMAIL_AUTH_CODE.getStatus().value()))
                .andExpect(jsonPath("$.message").value(INVALID_EMAIL_AUTH_CODE.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 가입 성공")
    void signUp_Success() throws Exception {
        SignUpRequest request = SignUpRequest.builder()
                .email("test@test.com")
                .nickname("광어우럭")
                .name("이름")
                .password("password1!")
                .emailAuth(true)
                .birth(LocalDate.of(2000, 1, 1))
                .build();

        SignUpResponse response = SignUpResponse.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .build();

        given(memberService.signUp(any())).willReturn(response);

        mockMvc.perform(post("/api/members/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(response.getEmail()))
                .andExpect(jsonPath("$.nickname").value(response.getNickname()))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 가입 실패 - 이메일 인증 X")
    void signUp_NoAuth_Fail() throws Exception {
        SignUpRequest request = SignUpRequest.builder()
                .email("test@test.com")
                .nickname("광어우럭")
                .name("이름")
                .password("password1!")
                .emailAuth(false)
                .birth(LocalDate.of(2000, 1, 1))
                .build();

        given(memberService.signUp(any())).willThrow(new MemberException(NOT_FINISH_EMAIL_AUTH));

        mockMvc.perform(post("/api/members/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(NOT_FINISH_EMAIL_AUTH.getStatus().value()))
                .andExpect(jsonPath("$.message").value(NOT_FINISH_EMAIL_AUTH.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 가입 실패 - 이메일 중복")
    void signUp_DuplicatedEmail_Fail() throws Exception {
        SignUpRequest request = SignUpRequest.builder()
                .email("test@test.com")
                .nickname("광어우럭")
                .name("이름")
                .password("password1!")
                .emailAuth(false)
                .birth(LocalDate.of(2000, 1, 1))
                .build();

        given(memberService.signUp(any())).willThrow(new MemberException(DUPLICATED_EMAIL));

        mockMvc.perform(post("/api/members/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(DUPLICATED_EMAIL.getStatus().value()))
                .andExpect(jsonPath("$.message").value(DUPLICATED_EMAIL.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 가입 실패 - 닉네임 중복")
    void signUp_DuplicatedNickname_Fail() throws Exception {
        SignUpRequest request = SignUpRequest.builder()
                .email("test@test.com")
                .nickname("광어우럭")
                .name("이름")
                .password("password1!")
                .emailAuth(false)
                .birth(LocalDate.of(2000, 1, 1))
                .build();

        given(memberService.signUp(any())).willThrow(new MemberException(DUPLICATED_NICKNAME));

        mockMvc.perform(post("/api/members/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(DUPLICATED_NICKNAME.getStatus().value()))
                .andExpect(jsonPath("$.message").value(DUPLICATED_NICKNAME.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 로그인 성공")
    void login_Success() throws Exception {

        LoginRequest request = LoginRequest.builder()
                .email("test@test.com")
                .password("password123!")
                .build();

        LoginResponse response = LoginResponse.builder()
                .refreshToken("refresh-token")
                .accessToken("access-token")
                .build();

        given(memberService.login(any())).willReturn(response);

        mockMvc.perform(post("/api/members/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(response.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(response.getRefreshToken()))
                .andDo(print());
    }

    @Test
    @DisplayName("access token 재발급 성공")
    void reissueToken_Success() throws Exception {
        ReissueTokenRequest request = ReissueTokenRequest.builder()
                .refreshToken("refresh-token")
                .build();

        ReissueTokenResponse response = ReissueTokenResponse.builder()
                .accessToken("access-token")
                .build();

        given(memberService.reissueToken(any())).willReturn(response);

        mockMvc.perform(post("/api/members/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(response.getAccessToken()))
                .andDo(print());
    }

    @Test
    @DisplayName("access token 재발급 실패 - 토큰 유효 X")
    void reissueToken_Fail_Invalid() throws Exception {
        ReissueTokenRequest request = ReissueTokenRequest.builder()
                .refreshToken("refresh-token")
                .build();

        given(memberService.reissueToken(any())).willThrow(new MemberAuthException(INVALID_REFRESH_TOKEN));

        mockMvc.perform(post("/api/members/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(INVALID_REFRESH_TOKEN.getStatus().value()))
                .andExpect(jsonPath("$.message").value(INVALID_REFRESH_TOKEN.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("access token 재발급 실패 - 토큰 존재 X")
    void reissueToken_Fail_NotExist() throws Exception {
        ReissueTokenRequest request = ReissueTokenRequest.builder()
                .refreshToken("refresh-token")
                .build();

        given(memberService.reissueToken(any())).willThrow(new MemberAuthException(NOT_EXIST_REFRESH_TOKEN));

        mockMvc.perform(post("/api/members/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(NOT_EXIST_REFRESH_TOKEN.getStatus().value()))
                .andExpect(jsonPath("$.message").value(NOT_EXIST_REFRESH_TOKEN.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("access token 재발급 실패 - 일치 X")
    void reissueToken_Fail_NotMatch() throws Exception {
        ReissueTokenRequest request = ReissueTokenRequest.builder()
                .refreshToken("refresh-token")
                .build();

        given(memberService.reissueToken(any())).willThrow(new MemberAuthException(NOT_MATCH_REFRESH_TOKEN));

        mockMvc.perform(post("/api/members/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(NOT_MATCH_REFRESH_TOKEN.getStatus().value()))
                .andExpect(jsonPath("$.message").value(NOT_MATCH_REFRESH_TOKEN.getMessage()))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("로그아웃 성공")
    void logout_Success() throws Exception {
        LogoutRequest request = LogoutRequest.builder()
                .accessToken("access-token")
                .build();

        Map<String, String> response = new HashMap<>();
        response.put("message", "로그아웃");

        given(memberService.logout(any())).willReturn(response);

        mockMvc.perform(post("/api/members/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(response.get("message")))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("로그아웃 실패")
    void logout_Fail() throws Exception {
        LogoutRequest request = LogoutRequest.builder()
                .accessToken("access-token")
                .build();

        given(memberService.logout(any())).willThrow(new MemberAuthException(INVALID_ACCESS_TOKEN));

        mockMvc.perform(post("/api/members/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(INVALID_ACCESS_TOKEN.getStatus().value()))
                .andExpect(jsonPath("$.message").value(INVALID_ACCESS_TOKEN.getMessage()))
                .andDo(print());
    }
}