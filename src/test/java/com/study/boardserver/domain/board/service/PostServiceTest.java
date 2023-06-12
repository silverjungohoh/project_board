package com.study.boardserver.domain.board.service;

import com.study.boardserver.domain.awss3.service.AwsS3Service;
import com.study.boardserver.domain.board.dto.post.PostWriteRequest;
import com.study.boardserver.domain.board.dto.post.PostWriteResponse;
import com.study.boardserver.domain.board.entity.Post;
import com.study.boardserver.domain.board.entity.PostImage;
import com.study.boardserver.domain.board.repository.PostImageRepository;
import com.study.boardserver.domain.board.repository.PostRepository;
import com.study.boardserver.domain.member.entity.Member;
import com.study.boardserver.global.error.exception.MemberAuthException;
import com.study.boardserver.global.error.type.MemberAuthErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private AwsS3Service awsS3Service;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    @DisplayName("회원 게시물 작성 실패")
    void writePost_Fail() {

        PostWriteRequest request = PostWriteRequest.builder()
                .title("제목입니다")
                .content("내용입니다")
                .build();

        String contentType = "image/png";

        MockMultipartFile file1 = new MockMultipartFile("test", "test_2023.png", contentType, "test".getBytes());

        MemberAuthException exception = assertThrows(MemberAuthException.class,
                () -> postService.writePost(null, request, List.of(file1))
        );

        assertEquals(MemberAuthErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원 게시물 작성 성공")
    void writePost_Success() {

        Member member = Member.builder()
                .id(1L)
                .email("email@gmail.com")
                .nickname("닉네임")
                .build();

        PostWriteRequest request = PostWriteRequest.builder()
                .title("제목입니다")
                .content("내용입니다")
                .build();

        String contentType = "image/png";

        MockMultipartFile file1 = new MockMultipartFile("test1", "test1_2023.png", contentType, "test1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("test2", "test2_2023.png", contentType, "test2".getBytes());

        String imgUrl1 = "https://image-bucket.s3.abc1.jpg";
        String imgUrl2 = "https://image-bucket.s3.abc2.jpg";

        given(awsS3Service.uploadFiles(anyList(), anyString())).willReturn(List.of(imgUrl1, imgUrl2));

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        ArgumentCaptor<PostImage> imageCaptor = ArgumentCaptor.forClass(PostImage.class);

        PostWriteResponse response = postService.writePost(member, request, List.of(file1, file2));

        assertEquals(response.getTitle(), request.getTitle());
        assertEquals(response.getContent(), request.getContent());
        assertEquals(response.getNickname(), member.getNickname());
        verify(postRepository, times(1)).save(postCaptor.capture());
        verify(postImageRepository, times(2)).save(imageCaptor.capture());
    }
}