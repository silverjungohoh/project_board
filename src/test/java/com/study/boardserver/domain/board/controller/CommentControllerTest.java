package com.study.boardserver.domain.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.boardserver.domain.board.dto.comment.CommentUpdateRequest;
import com.study.boardserver.domain.board.dto.comment.CommentUpdateResponse;
import com.study.boardserver.domain.board.dto.comment.CommentWriteRequest;
import com.study.boardserver.domain.board.dto.comment.CommentWriteResponse;
import com.study.boardserver.domain.board.service.CommentService;
import com.study.boardserver.domain.member.entity.Member;
import com.study.boardserver.domain.member.repository.MemberRepository;
import com.study.boardserver.domain.member.type.MemberRole;
import com.study.boardserver.domain.security.CustomUserDetails;
import com.study.boardserver.global.error.exception.BoardException;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.study.boardserver.global.error.type.BoardErrorCode.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @MockBean
    private CommentService commentService;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .id(1L)
                .email("test123@test.com")
                .nickname("nickname")
                .role(MemberRole.ROLE_USER)
                .build();

        memberRepository.save(member);
        userDetails = new CustomUserDetails(memberRepository.findById(1L).get());
    }

    @Test
    @DisplayName("댓글 작성 실패 - 게시물 없음")
    void writeComment_Fail_NoPost() throws Exception {

        CommentWriteRequest request = CommentWriteRequest.builder()
                .content("댓글 내용")
                .build();

        given(commentService.writeComment(any(), anyLong(), any())).willThrow(new BoardException(POST_NOT_FOUND));

        mockMvc.perform(post("/api/boards/{postId}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(POST_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(POST_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 작성 성공")
    void writeComment_Success() throws Exception{

        CommentWriteRequest request = CommentWriteRequest.builder()
                .content("댓글 내용")
                .build();

        LocalDateTime createdAt = LocalDateTime.of(2023, 6, 10, 10, 30);

        CommentWriteResponse response = CommentWriteResponse.builder()
                .commentId(1L)
                .nickname("닉네임")
                .content("댓글 작성")
                .createdAt(createdAt)
                .build();

        given(commentService.writeComment(any(), anyLong(), any())).willReturn(response);

        mockMvc.perform(post("/api/boards/{postId}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentId").value(response.getCommentId()))
                .andExpect(jsonPath("$.nickname").value(response.getNickname()))
                .andExpect(jsonPath("$.content").value(response.getContent()))
                .andExpect(jsonPath("$.createdAt").value(response.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 게시물 없음")
    void updateComment_Fail_NoPost() throws Exception{

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content("댓글 내용")
                .build();

        given(commentService.updateComment(any(), anyLong(), anyLong(), any())).willThrow(new BoardException(POST_NOT_FOUND));

        mockMvc.perform(patch("/api/boards/{postId}/comments/{commentId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(POST_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(POST_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 댓글 없음")
    void updateComment_Fail_NoComment() throws Exception{

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content("댓글 내용")
                .build();

        given(commentService.updateComment(any(), anyLong(), anyLong(), any())).willThrow(new BoardException(COMMENT_NOT_FOUND));

        mockMvc.perform(patch("/api/boards/{postId}/comments/{commentId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(COMMENT_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(COMMENT_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 작성자 아님")
    void updateComment_Fail_NotMatch() throws Exception{

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content("댓글 내용")
                .build();

        given(commentService.updateComment(any(), anyLong(), anyLong(), any())).willThrow(new BoardException(CANNOT_UPDATE_COMMENT));

        mockMvc.perform(patch("/api/boards/{postId}/comments/{commentId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(CANNOT_UPDATE_COMMENT.getStatus().value()))
                .andExpect(jsonPath("$.message").value(CANNOT_UPDATE_COMMENT.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_Success() throws Exception{

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content("댓글 내용")
                .build();

        LocalDateTime createdAt = LocalDateTime.of(2023, 6, 10, 10, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2023, 6, 11, 10, 30);

        CommentUpdateResponse response = CommentUpdateResponse.builder()
                .commentId(1L)
                .nickname("닉네임")
                .content("댓글 작성")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        given(commentService.updateComment(any(), anyLong(), anyLong(), any())).willReturn(response);

        mockMvc.perform(patch("/api/boards/{postId}/comments/{commentId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(response.getCommentId()))
                .andExpect(jsonPath("$.nickname").value(response.getNickname()))
                .andExpect(jsonPath("$.content").value(response.getContent()))
                .andExpect(jsonPath("$.createdAt").value(response.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .andExpect(jsonPath("$.updatedAt").value(response.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 게시물 없음")
    void deleteComment_Fail_NoPost() throws Exception {

        given(commentService.deleteComment(any(), anyLong(), anyLong())).willThrow(new BoardException(POST_NOT_FOUND));

        mockMvc.perform(delete("/api/boards/{postId}/comments/{commentId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(POST_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(POST_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 댓글 없음")
    void deleteComment_Fail_NoComment() throws Exception {

        given(commentService.deleteComment(any(), anyLong(), anyLong())).willThrow(new BoardException(POST_NOT_FOUND));

        mockMvc.perform(delete("/api/boards/{postId}/comments/{commentId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(COMMENT_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(POST_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 작성자 아님")
    void deleteComment_Fail_NotMatch() throws Exception {

        given(commentService.deleteComment(any(), anyLong(), anyLong())).willThrow(new BoardException(CANNOT_DELETE_COMMENT));

        mockMvc.perform(delete("/api/boards/{postId}/comments/{commentId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(CANNOT_DELETE_COMMENT.getStatus().value()))
                .andExpect(jsonPath("$.message").value(CANNOT_DELETE_COMMENT.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_Success() throws Exception {

        Map<String, String> response = new HashMap<>();
        response.put("message", "댓글이 삭제되었습니다.");

        given(commentService.deleteComment(any(), anyLong(), anyLong())).willReturn(response);

        mockMvc.perform(delete("/api/boards/{postId}/comments/{commentId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(response.get("message")))
                .andDo(print());
    }
}