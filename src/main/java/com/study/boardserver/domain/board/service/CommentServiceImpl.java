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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    @Override
    public CommentUpdateResponse updateComment(Member member, Long postId, Long commentId, CommentUpdateRequest request) {

        if(!postRepository.existsById(postId)) {
            throw new BoardException(BoardErrorCode.POST_NOT_FOUND);
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.COMMENT_NOT_FOUND));

        if(!Objects.equals(member.getEmail(), comment.getMember().getEmail())) {
            throw new BoardException(BoardErrorCode.CANNOT_UPDATE_COMMENT);
        }

        comment.update(request.getContent());
        commentRepository.save(comment);

        return CommentUpdateResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .nickname(member.getNickname())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    @Override
    public Map<String, String> deleteComment(Member member, Long postId, Long commentId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.COMMENT_NOT_FOUND));

        if(!Objects.equals(member.getEmail(), comment.getMember().getEmail())) {
            throw new BoardException(BoardErrorCode.CANNOT_DELETE_COMMENT);
        }

        commentRepository.delete(comment);
        post.removeComment(comment);
        return getMessage("댓글이 삭제되었습니다.");
    }

    private static Map<String, String> getMessage(String message) {
        Map<String, String> result = new HashMap<>();
        result.put("message", message);
        return result;
    }
}
