package com.study.boardserver.domain.board.controller;

import com.study.boardserver.domain.board.dto.post.PostWriteRequest;
import com.study.boardserver.domain.board.dto.post.PostWriteResponse;
import com.study.boardserver.domain.board.service.PostService;
import com.study.boardserver.domain.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

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
}
