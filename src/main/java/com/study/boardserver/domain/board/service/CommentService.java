package com.study.boardserver.domain.board.service;

import com.study.boardserver.domain.board.dto.comment.CommentUpdateRequest;
import com.study.boardserver.domain.board.dto.comment.CommentUpdateResponse;
import com.study.boardserver.domain.board.dto.comment.CommentWriteRequest;
import com.study.boardserver.domain.board.dto.comment.CommentWriteResponse;
import com.study.boardserver.domain.member.entity.Member;

public interface CommentService {

    /**
     * 댓글 작성
     */
    CommentWriteResponse writeComment(Member member, Long postId, CommentWriteRequest request);

    /**
     * 댓글 수정
     */
    CommentUpdateResponse updateComment(Member member, Long postId, Long commentId, CommentUpdateRequest request);
}
