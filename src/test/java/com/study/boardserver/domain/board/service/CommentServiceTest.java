package com.study.boardserver.domain.board.service;

import com.study.boardserver.domain.board.dto.comment.CommentWriteRequest;
import com.study.boardserver.domain.board.dto.comment.CommentWriteResponse;
import com.study.boardserver.domain.board.entity.Comment;
import com.study.boardserver.domain.board.entity.Post;
import com.study.boardserver.domain.board.repository.CommentRepository;
import com.study.boardserver.domain.board.repository.PostRepository;
import com.study.boardserver.domain.member.entity.Member;
import com.study.boardserver.global.error.exception.BoardException;
import com.study.boardserver.global.error.type.BoardErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentServiceImpl commentService;


    @Test
    @DisplayName("댓글 작성 실패 - 게시물 없음")
    void writeComment_Fail_NoPost() {

        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("nickname")
                .build();

        CommentWriteRequest request = CommentWriteRequest.builder()
                .content("댓글 내용")
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        BoardException exception = assertThrows(BoardException.class,
                () -> commentService.writeComment(member, 1L, request));

        assertEquals(BoardErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 작성 성공")
    void writeComment_Success() {
        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("nickname")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("제목")
                .content("내용")
                .build();

        CommentWriteRequest request = CommentWriteRequest.builder()
                .content("댓글 내용")
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        CommentWriteResponse response = commentService.writeComment(member, 1L, request);

        assertEquals(response.getContent(), request.getContent());
        assertEquals(response.getNickname(), member.getNickname());
        verify(commentRepository, times(1)).save(commentCaptor.capture());
    }
}