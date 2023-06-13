package com.study.boardserver.domain.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.boardserver.domain.board.dto.post.*;
import com.study.boardserver.domain.board.service.PostService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.study.boardserver.global.error.type.BoardErrorCode.*;
import static org.mockito.ArgumentMatchers.*;
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
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @MockBean
    private PostService postService;

    private Member member;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test123@test.com")
                .nickname("nickname")
                .role(MemberRole.ROLE_USER)
                .build();

        memberRepository.save(member);
        userDetails = new CustomUserDetails(memberRepository.findById(1L).get());
    }

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

    @Test
    @DisplayName("게시물 작성 성공")
    void writePost_Success() throws Exception {

        String contentType = "image/png";

        MockMultipartFile file1 = new MockMultipartFile("test1", "test1_2023.png", contentType, "test1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("test2", "test2_2023.png", contentType, "test2".getBytes());

        PostWriteRequest request = PostWriteRequest.builder()
                .title("제목입니다")
                .content("내용입니다")
                .build();

        String imgUrl1 = "https://image-bucket.s3.abc1.jpg";
        String imgUrl2 = "https://image-bucket.s3.abc2.jpg";

        PostImageUrlResponse imgResponse1 = PostImageUrlResponse.builder()
                .imageId(1L)
                .imageUrl(imgUrl1)
                .build();

        PostImageUrlResponse imgResponse2 = PostImageUrlResponse.builder()
                .imageId(2L)
                .imageUrl(imgUrl2)
                .build();

        LocalDateTime date = LocalDateTime.of(2023, 6, 12, 23, 59, 59);

        PostWriteResponse response = PostWriteResponse.builder()
                .postId(1L)
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrls(List.of(imgResponse1, imgResponse2))
                .nickname(member.getNickname())
                .createdAt(date)
                .build();

        given(postService.writePost(any(), any(), anyList())).willReturn(response);

        String requestJson = objectMapper.writeValueAsString(request);
        MockPart data = new MockPart("post", requestJson.getBytes());
        data.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart("/api/boards")
                        .file("images", file1.getBytes())
                        .file("images", file2.getBytes())
                        .part(data)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").value(response.getPostId()))
                .andExpect(jsonPath("$.title").value(response.getTitle()))
                .andExpect(jsonPath("$.content").value(response.getContent()))
                .andExpect(jsonPath("$.imageUrls[0].imageUrl").value(
                        response.getImageUrls().get(0).getImageUrl()))
                .andExpect(jsonPath("$.nickname").value(response.getNickname()))
                .andExpect(jsonPath("$.createdAt").value(response.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .andDo(print());
    }

    @Test
    @DisplayName("게시물 수정 성공")
    void updatePost_Success() throws Exception {
        PostUpdateRequest request = PostUpdateRequest.builder()
                .title("제목입니다")
                .content("내용입니다")
                .build();

        LocalDateTime createdAt = LocalDateTime.of(2023, 6, 12, 23, 59, 59);
        LocalDateTime updatedAt = LocalDateTime.of(2023, 6, 15, 23, 59, 59);

        String imgUrl1 = "https://image-bucket.s3.abc1.jpg";
        String imgUrl2 = "https://image-bucket.s3.abc2.jpg";

        PostImageUrlResponse imgResponse1 = PostImageUrlResponse.builder()
                .imageId(1L)
                .imageUrl(imgUrl1)
                .build();

        PostImageUrlResponse imgResponse2 = PostImageUrlResponse.builder()
                .imageId(2L)
                .imageUrl(imgUrl2)
                .build();

        PostUpdateResponse response = PostUpdateResponse.builder()
                .postId(1L)
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrls(List.of(imgResponse1, imgResponse2))
                .nickname(userDetails.getMember().getNickname())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        given(postService.updatePost(any(), anyLong(), any())).willReturn(response);

        mockMvc.perform(patch("/api/boards/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(response.getPostId()))
                .andExpect(jsonPath("$.title").value(response.getTitle()))
                .andExpect(jsonPath("$.content").value(response.getContent()))
                .andExpect(jsonPath("$.imageUrls[0].imageUrl").value(
                        response.getImageUrls().get(0).getImageUrl()))
                .andExpect(jsonPath("$.nickname").value(response.getNickname()))
                .andExpect(jsonPath("$.createdAt").value(response.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .andExpect(jsonPath("$.updatedAt").value(response.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .andDo(print());
    }

    @Test
    @DisplayName("게시물 수정 실패 - 수정 권한 없음")
    void updatePost_Fail_NotMatch() throws Exception {

        PostUpdateRequest request = PostUpdateRequest.builder()
                .title("제목입니다")
                .content("내용입니다")
                .build();

        given(postService.updatePost(any(), anyLong(), any())).willThrow(new BoardException(CANNOT_UPDATE_POST));

        mockMvc.perform(patch("/api/boards/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(CANNOT_UPDATE_POST.getStatus().value()))
                .andExpect(jsonPath("$.message").value(CANNOT_UPDATE_POST.getMessage()))
                .andDo(print());
    }

    @Test
    @DisplayName("게시물 수정 실패 - 게시물 없음")
    void updatePost_Fail_NoPost() throws Exception {

        PostUpdateRequest request = PostUpdateRequest.builder()
                .title("제목입니다")
                .content("내용입니다")
                .build();

        given(postService.updatePost(any(), anyLong(), any())).willThrow(new BoardException(POST_NOT_FOUND));

        mockMvc.perform(patch("/api/boards/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(POST_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(POST_NOT_FOUND.getMessage()))
                .andDo(print());
    }


}