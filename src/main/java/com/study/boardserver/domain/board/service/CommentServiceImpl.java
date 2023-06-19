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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    public CommentWriteResponse writeComment(Member member, Long postId, CommentWriteRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .member(member)
                .build();

        commentRepository.save(comment);
        post.addComment(comment);

        return CommentWriteResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .nickname(member.getNickname())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
