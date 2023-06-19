package com.study.boardserver.domain.board.service;

import com.study.boardserver.domain.board.dto.comment.CommentUpdateRequest;
import com.study.boardserver.domain.board.dto.comment.CommentUpdateResponse;
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

    @Test
    @DisplayName("댓글 수정 실패 - 게시물 없음")
    void updateComment_Fail_NoPost() {

        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("nickname")
                .build();

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content("댓글 수정")
                .build();

        given(postRepository.existsById(anyLong())).willReturn(false);

        BoardException exception = assertThrows(BoardException.class,
                () -> commentService.updateComment(member, 1L, 1L, request));

        assertEquals(BoardErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 댓글 없음")
    void updateComment_Fail_NoComment() {

        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("nickname")
                .build();

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content("댓글 수정")
                .build();

        given(postRepository.existsById(anyLong())).willReturn(true);
        given(commentRepository.findById(anyLong())).willReturn(Optional.empty());

        BoardException exception = assertThrows(BoardException.class,
                () -> commentService.updateComment(member, 1L, 1L, request));

        assertEquals(BoardErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 작성자 아님")
    void updateComment_Fail_NotMatch() {

        Member member1 = Member.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("nickname1")
                .build();

        Member member2 = Member.builder()
                .id(2L)
                .email("test234@test.com")
                .nickname("nickname2")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("제목입니다")
                .content("내용입니다")
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .content("댓글")
                .member(member1)
                .post(post)
                .build();

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content("댓글 수정")
                .build();

        given(postRepository.existsById(anyLong())).willReturn(true);
        given(commentRepository.findById(anyLong())).willReturn(Optional.of(comment));

        BoardException exception = assertThrows(BoardException.class,
                () -> commentService.updateComment(member2, 1L, 1L, request));

        assertEquals(BoardErrorCode.CANNOT_UPDATE_COMMENT, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_Success() {

        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("nickname")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("제목입니다")
                .content("내용입니다")
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .content("댓글")
                .member(member)
                .post(post)
                .build();

        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content("댓글 수정")
                .build();

        given(postRepository.existsById(anyLong())).willReturn(true);
        given(commentRepository.findById(anyLong())).willReturn(Optional.of(comment));

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        CommentUpdateResponse response = commentService.updateComment(member, 1L, 1L, request);

        assertEquals(response.getContent(), request.getContent());
        assertEquals(response.getNickname(), member.getNickname());
        verify(commentRepository, times(1)).save(commentCaptor.capture());
    }
}