package com.study.boardserver.domain.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.boardserver.domain.board.dto.post.PostImageUrlResponse;
import com.study.boardserver.domain.board.service.PostService;
import com.study.boardserver.global.error.exception.BoardException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static com.study.boardserver.global.error.type.BoardErrorCode.POST_IMAGE_NOT_FOUND;
import static com.study.boardserver.global.error.type.BoardErrorCode.POST_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;


    @Test
    @WithMockUser
    @DisplayName("게시물 이미지 업로드 성공")
    void uploadPostImage_Success() throws Exception {
        String contentType = "image/png";
        MockMultipartFile file = new MockMultipartFile("test", "test_2023.png", contentType, "test".getBytes());

        PostImageUrlResponse response = PostImageUrlResponse.builder()
                .imageId(1L)
                .imageUrl("https://image-bucket.s3.abc1.jpg")
                .build();

        given(postService.uploadPostImage(anyLong(), any())).willReturn(response);

        mockMvc.perform(multipart("/api/boards/{postId}/images", 1L)
                        .file("image", file.getBytes())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("Authorization", "accessToken")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageId").value(response.getImageId()))
                .andExpect(jsonPath("$.imageUrl").value(response.getImageUrl()))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("이미지 업로드 실패 - 게시물 없음")
    void uploadPostImage_Fail_NoPost() throws Exception {
        String contentType = "image/png";
        MockMultipartFile file = new MockMultipartFile("test", "test_2023.png", contentType, "test".getBytes());

        given(postService.uploadPostImage(anyLong(), any())).willThrow(new BoardException(POST_NOT_FOUND));

        mockMvc.perform(multipart("/api/boards/{postId}/images", 1L)
                        .file("image", file.getBytes())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("Authorization", "accessToken")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(POST_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(POST_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("이미지 삭제 성공")
    void deletePostImage_Success() throws Exception {

        Map<String, String> response = new HashMap<>();
        response.put("message", "이미지가 삭제되었습니다.");

        given(postService.deletePostImage(anyLong(), anyLong())).willReturn(response);

        mockMvc.perform(delete("/api/boards/{postId}/images/{postImageId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "accessToken")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(response.get("message")))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("이미지 삭제 실패 - 게시물 없음")
    void deletePostImage_Fail_NoPost() throws Exception {

        given(postService.deletePostImage(anyLong(), anyLong())).willThrow(new BoardException(POST_NOT_FOUND));

        mockMvc.perform(delete("/api/boards/{postId}/images/{postImageId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "accessToken")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(POST_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(POST_NOT_FOUND.getMessage()))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("이미지 삭제 실패 - 이미지 없음")
    void deletePostImage_Fail_NoPostImage() throws Exception {

        given(postService.deletePostImage(anyLong(), anyLong())).willThrow(new BoardException(POST_IMAGE_NOT_FOUND));

        mockMvc.perform(delete("/api/boards/{postId}/images/{postImageId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "accessToken")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(POST_IMAGE_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(POST_IMAGE_NOT_FOUND.getMessage()))
                .andDo(print());
    }
}