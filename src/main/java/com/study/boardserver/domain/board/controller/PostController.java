package com.study.boardserver.domain.board.controller;

import com.study.boardserver.domain.board.dto.post.*;
import com.study.boardserver.domain.board.service.PostService;
import com.study.boardserver.domain.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/boards")
@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "게시판 글 작성")
    public ResponseEntity<PostWriteResponse> writePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @RequestPart(value = "post") @Valid PostWriteRequest request,
                                                       @RequestPart(value = "images", required = false) List<MultipartFile> files) {

        PostWriteResponse response = postService.writePost(userDetails.getMember(), request, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시판 글 삭제")
    public ResponseEntity<Map<String, String>> deletePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                          @PathVariable Long postId) {

        Map<String, String> result = postService.deletePost(userDetails.getMember(), postId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{postId}/images")
    @Operation(summary = "이미지 등록")
    public ResponseEntity<PostImageUrlResponse> uploadPostImage(@PathVariable Long postId,
                                                                @RequestPart(value = "image", required = false) MultipartFile file) {

        PostImageUrlResponse response = postService.uploadPostImage(postId, file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}/images/{postImageId}")
    @Operation(summary = "이미지 삭제")
    public ResponseEntity<Map<String, String>> deletePostImage(@PathVariable Long postId, @PathVariable Long postImageId) {

        Map<String, String> result = postService.deletePostImage(postId, postImageId);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{postId}")
    @Operation(summary = "게시판 글 수정")
    public ResponseEntity<PostUpdateResponse> updatePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                         @RequestBody @Valid PostUpdateRequest request,
                                                         @PathVariable Long postId) {

        PostUpdateResponse response = postService.updatePost(userDetails.getMember(), postId, request);
        return ResponseEntity.ok(response);
    }

}
