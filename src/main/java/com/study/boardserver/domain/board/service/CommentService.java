package com.study.boardserver.domain.board.service;

import com.study.boardserver.domain.board.dto.comment.CommentWriteRequest;
import com.study.boardserver.domain.board.dto.comment.CommentWriteResponse;
import com.study.boardserver.domain.member.entity.Member;

public interface CommentService {

    /**
     * 댓글 작성
     */
    CommentWriteResponse writeComment(Member member, Long postId, CommentWriteRequest request);
}
