package com.study.boardserver.domain.board.service;

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
    public Map<String, String> pushHeart(Member member, long postId) {

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

    private static Map<String, String> getMessage(String message) {
        Map<String, String> result = new HashMap<>();
        result.put("message", message);
        return result;
    }
}
