package com.study.boardserver.domain.board.controller;

import com.study.boardserver.domain.board.service.HeartService;
import com.study.boardserver.domain.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class HeartController {

    private final HeartService heartService;

    @PostMapping("/{postId}/hearts")
    public ResponseEntity<Map<String, String>> pushHeart(@PathVariable Long postId,
                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<String, String> result = heartService.pushHeart(userDetails.getMember(), postId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{postId}/hearts")
    public ResponseEntity<Map<String, String>> deleteHeart(@PathVariable Long postId,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<String, String> result = heartService.deleteHeart(userDetails.getMember(), postId);
        return ResponseEntity.ok(result);
    }
}
