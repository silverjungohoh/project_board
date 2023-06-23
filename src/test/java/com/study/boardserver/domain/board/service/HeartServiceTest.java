package com.study.boardserver.domain.board.service;

import com.study.boardserver.domain.board.dto.heart.HeartCountGetResponse;
import com.study.boardserver.domain.board.entity.Heart;
import com.study.boardserver.domain.board.entity.Post;
import com.study.boardserver.domain.board.repository.HeartRepository;
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

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HeartServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private HeartRepository heartRepository;

    @InjectMocks
    private HeartServiceImpl heartService;

    @Test
    @DisplayName("좋아요 등록 성공")
    void pushHeart_Success() {

        Member member1 = Member.builder()
                .id(1L)
                .email("test1@test.com")
                .build();

        Member member2 = Member.builder()
                .id(2L)
                .email("test2@test.com")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("제목")
                .content("내용")
                .member(member1)
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        given(heartRepository.existsByPostAndMember(any(), any())).willReturn(false);

        ArgumentCaptor<Heart> heartCaptor = ArgumentCaptor.forClass(Heart.class);

        Map<String, String> result = heartService.pushHeart(member2, 1L);

        assertNotNull(result.get("message"));
        verify(heartRepository, times(1)).save(heartCaptor.capture());
    }

    @Test
    @DisplayName("좋아요 등록 실패 - 게시물 없음")
    void pushHeart_Fail_NoPost() {

        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        BoardException exception = assertThrows(BoardException.class,
                () -> heartService.pushHeart(member, 1L));

        assertEquals(BoardErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 등록 실패 - 자신의 게시물")
    void pushHeart_Fail_MyPost() {

        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("제목")
                .content("내용")
                .member(member)
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        given(heartRepository.existsByPostAndMember(any(), any())).willReturn(false);

        BoardException exception = assertThrows(BoardException.class,
                () -> heartService.pushHeart(member, 1L));

        assertEquals(BoardErrorCode.CANNOT_PUSH_HEART, exception.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 등록 실패 - 이미 누름")
    void pushHeart_Fail_AlreadyExist() {

        Member member1 = Member.builder()
                .id(1L)
                .email("test1@test.com")
                .build();

        Member member2 = Member.builder()
                .id(2L)
                .email("test2@test.com")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("제목")
                .content("내용")
                .member(member1)
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        given(heartRepository.existsByPostAndMember(any(), any())).willReturn(true);

        BoardException exception = assertThrows(BoardException.class,
                () -> heartService.pushHeart(member2, 1L));

        assertEquals(BoardErrorCode.ALREADY_PUSH_HEART, exception.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 취소 성공")
    void deleteHeart_Success() {

        Member member1 = Member.builder()
                .id(1L)
                .email("test1@test.com")
                .build();

        Member member2 = Member.builder()
                .id(2L)
                .email("test2@test.com")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("제목")
                .content("내용")
                .member(member1)
                .build();

        Heart heart = Heart.builder()
                .id(1L)
                .member(member2)
                .post(post)
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        given(heartRepository.findByPostAndMember(any(), any())).willReturn(Optional.of(heart));

        ArgumentCaptor<Heart> heartCaptor = ArgumentCaptor.forClass(Heart.class);

        Map<String, String> result = heartService.deleteHeart(member2, 1L);

        assertNotNull(result.get("message"));
        verify(heartRepository, times(1)).delete(heartCaptor.capture());
    }

    @Test
    @DisplayName("좋아요 취소 실패 - 게시물 없음")
    void deleteHeart_Fail_NoPost() {

        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        BoardException exception = assertThrows(BoardException.class,
                () -> heartService.deleteHeart(member, 1L));

        assertEquals(BoardErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 취소 실패 - 좋아요 없음")
    void deleteHeart_Fail_NoHeart() {

        Member member1 = Member.builder()
                .id(1L)
                .email("test1@test.com")
                .build();

        Member member2 = Member.builder()
                .id(2L)
                .email("test2@test.com")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("제목")
                .content("내용")
                .member(member1)
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        given(heartRepository.findByPostAndMember(any(), any())).willReturn(Optional.empty());

        BoardException exception = assertThrows(BoardException.class,
                () -> heartService.deleteHeart(member2, 1L));

        assertEquals(BoardErrorCode.HEART_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시물 좋아요 개수 조회 성공")
    void getHeartCountByPost_Success() {

        Post post = Post.builder()
                .id(1L)
                .title("제목")
                .content("내용")
                .build();

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        given(heartRepository.countByPost(any())).willReturn(100L);

        HeartCountGetResponse response = heartService.getHeartCountByPost(1L);

        assertEquals(response.getPostId(), 1L);
        assertEquals(response.getHeartCnt(), 100L);
    }

    @Test
    @DisplayName("게시물 좋아요 개수 조회 실패 - 게시물 없음")
    void getHeartCountByPost_Fail_NoPost() {

        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        BoardException exception = assertThrows(BoardException.class,
                () -> heartService.getHeartCountByPost(1L));

        assertEquals(BoardErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }
}