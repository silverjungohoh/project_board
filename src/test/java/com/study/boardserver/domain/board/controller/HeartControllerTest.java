package com.study.boardserver.domain.board.controller;

import com.study.boardserver.domain.board.service.HeartService;
import com.study.boardserver.domain.member.entity.Member;
import com.study.boardserver.domain.member.repository.MemberRepository;
import com.study.boardserver.domain.member.type.MemberRole;
import com.study.boardserver.domain.security.CustomUserDetails;
import com.study.boardserver.global.error.exception.BoardException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static com.study.boardserver.global.error.type.BoardErrorCode.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class HeartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @MockBean
    private HeartService heartService;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .id(1L)
                .email("test123@test.com")
                .nickname("nickname")
                .role(MemberRole.ROLE_USER)
                .build();

        memberRepository.saveAndFlush(member);
        userDetails = new CustomUserDetails(member);
    }

    @AfterEach
    void afterEach() {
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("좋아요 등록 성공")
    void pushHeart_Success() throws Exception {

        Map<String, String> response = new HashMap<>();
        response.put("message", "좋아요 등록");

        given(heartService.pushHeart(any(), anyLong())).willReturn(response);

        mockMvc.perform(post("/api/boards/{postId}/hearts", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(response.get("message")))
                .andDo(print());
    }

    @Test
    @DisplayName("좋아요 등록 실패 - 게시물 없음")
    void pushHeart_Fail_NoPost() throws Exception {
        given(heartService.pushHeart(any(), anyLong())).willThrow(new BoardException(POST_NOT_FOUND));

        mockMvc.perform(post("/api/boards/{postId}/hearts", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(POST_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(POST_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("좋아요 등록 실패 - 자신의 게시물")
    void pushHeart_Fail_MyPost() throws Exception {

        given(heartService.pushHeart(any(), anyLong())).willThrow(new BoardException(CANNOT_PUSH_HEART));

        mockMvc.perform(post("/api/boards/{postId}/hearts", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(CANNOT_PUSH_HEART.getStatus().value()))
                .andExpect(jsonPath("$.message").value(CANNOT_PUSH_HEART.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("좋아요 등록 실패 - 이미 누름")
    void pushHeart_Fail_AlreadyExist() throws Exception {

        given(heartService.pushHeart(any(), anyLong())).willThrow(new BoardException(ALREADY_PUSH_HEART));

        mockMvc.perform(post("/api/boards/{postId}/hearts", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(ALREADY_PUSH_HEART.getStatus().value()))
                .andExpect(jsonPath("$.message").value(ALREADY_PUSH_HEART.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("좋아요 취소 성공")
    void deleteHeart_Success() throws Exception {

        Map<String, String> response = new HashMap<>();
        response.put("message", "좋아요 취소");

        given(heartService.deleteHeart(any(), anyLong())).willReturn(response);

        mockMvc.perform(delete("/api/boards/{postId}/hearts", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(response.get("message")))
                .andDo(print());
    }

    @Test
    @DisplayName("좋아요 취소 실패 - 게시물 없음")
    void deleteHeart_Fail_NoPost() throws Exception {

        given(heartService.deleteHeart(any(), anyLong())).willThrow(new BoardException(POST_NOT_FOUND));

        mockMvc.perform(delete("/api/boards/{postId}/hearts", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(POST_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(POST_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("좋아요 취소 실패 - 좋아요 없음")
    void deleteHeart_Fail_NoHeart() throws Exception {

        given(heartService.deleteHeart(any(), anyLong())).willThrow(new BoardException(HEART_NOT_FOUND));

        mockMvc.perform(delete("/api/boards/{postId}/hearts", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HEART_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(HEART_NOT_FOUND.getMessage()))
                .andDo(print());
    }
}