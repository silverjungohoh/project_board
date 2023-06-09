package com.study.boardserver.domain.board.service;

import com.study.boardserver.domain.board.dto.heart.HeartCountGetResponse;
import com.study.boardserver.domain.board.entity.Heart;
import com.study.boardserver.domain.board.entity.Post;
import com.study.boardserver.domain.board.repository.HeartRepository;
import com.study.boardserver.domain.board.repository.PostRepository;
import com.study.boardserver.domain.member.entity.Member;
import com.study.boardserver.global.error.exception.BoardException;
import com.study.boardserver.global.error.type.BoardErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HeartServiceImpl implements HeartService {

    private final PostRepository postRepository;
    private final HeartRepository heartRepository;

    @Override
    public Map<String, String> pushHeart(Member member, Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        if(heartRepository.existsByPostAndMember(post, member)) {
            throw new BoardException(BoardErrorCode.ALREADY_PUSH_HEART);
        }

        if(Objects.equals(member.getEmail(), post.getMember().getEmail())) {
            throw new BoardException(BoardErrorCode.CANNOT_PUSH_HEART);
        }

        Heart heart = Heart.builder()
                .post(post)
                .member(member)
                .build();

        heartRepository.save(heart);

        return getMessage("좋아요 등록");
    }

    @Override
    public Map<String, String> deleteHeart(Member member, Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        Heart heart = heartRepository.findByPostAndMember(post, member)
                .orElseThrow(() -> new BoardException(BoardErrorCode.HEART_NOT_FOUND));

        heartRepository.delete(heart);

        return getMessage("좋아요 취소");
    }

    @Override
    public HeartCountGetResponse getHeartCountByPost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        return HeartCountGetResponse.builder()
                .postId(postId)
                .heartCnt(heartRepository.countByPost(post))
                .build();
    }

    private static Map<String, String> getMessage(String message) {
        Map<String, String> result = new HashMap<>();
        result.put("message", message);
        return result;
    }
}
