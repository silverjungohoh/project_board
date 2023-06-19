package com.study.boardserver.domain.board.controller;

import com.study.boardserver.domain.board.dto.comment.CommentUpdateRequest;
import com.study.boardserver.domain.board.dto.comment.CommentUpdateResponse;
import com.study.boardserver.domain.board.dto.comment.CommentWriteRequest;
import com.study.boardserver.domain.board.dto.comment.CommentWriteResponse;
import com.study.boardserver.domain.board.service.CommentService;
import com.study.boardserver.domain.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RequestMapping("/api/boards")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentWriteResponse> writeComment(@PathVariable Long postId,
                                                             @RequestBody @Valid CommentWriteRequest request,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {

        CommentWriteResponse response = commentService.writeComment(userDetails.getMember(), postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentUpdateResponse> updateComment(@PathVariable Long postId,
                                                               @PathVariable Long commentId,
                                                               @RequestBody @Valid CommentUpdateRequest request,
                                                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        CommentUpdateResponse response = commentService.updateComment(userDetails.getMember(), postId, commentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(@PathVariable Long postId,
                                                             @PathVariable Long commentId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<String, String> result = commentService.deleteComment(userDetails.getMember(), postId, commentId);
        return ResponseEntity.ok(result);
    }
}
