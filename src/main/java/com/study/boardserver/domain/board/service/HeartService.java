package com.study.boardserver.domain.board.service;

import com.study.boardserver.domain.member.entity.Member;

import java.util.Map;

public interface HeartService {

    /**
     * 좋아요 등록
     */
    Map<String, String> pushHeart(Member member, Long postId);

    /**
     * 좋아요 취소
     */
    Map<String, String> deleteHeart(Member member, Long postId);
}
