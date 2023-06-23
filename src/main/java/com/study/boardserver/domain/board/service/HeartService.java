package com.study.boardserver.domain.board.service;

import com.study.boardserver.domain.member.entity.Member;

import java.util.Map;

public interface HeartService {

    /**
     * 좋아요 등록
     */
    Map<String, String> pushHeart(Member member, long postId);
}
