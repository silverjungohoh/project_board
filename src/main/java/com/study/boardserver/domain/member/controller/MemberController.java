package com.study.boardserver.domain.member.controller;

import com.study.boardserver.domain.member.dto.signup.ConfirmAuthCodeRequest;
import com.study.boardserver.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "회원", description = "회원 API Document")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/sign-up/email")
    @Operation(summary = "이메일 중복 확인")
    public ResponseEntity<Map<String, String>> checkDuplicatedEmail(@RequestBody Map<String, String> email) {
        Map<String, String> result = memberService.checkDuplicatedEmail(email.get("email"));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/nickname")
    @Operation(summary = "닉네임 중복 확인")
    public ResponseEntity<Map<String, String>> checkDuplicatedNickname(@RequestBody Map<String, String> nickname) {
        Map<String, String> result = memberService.checkDuplicatedNickname(nickname.get("nickname"));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/email-auth")
    @Operation(summary = "이메일 인증 코드 발송")
    public ResponseEntity<Map<String, String>> sendAuthCode(@RequestBody Map<String, String> email) {
        Map<String, String> result = memberService.sendAuthCode(email.get("email"));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/email-authentication")
    @Operation(summary = "이메일 인증 코드 확인")
    public ResponseEntity<Map<String, String>> confirmAuthCode(@RequestBody ConfirmAuthCodeRequest request) {
        Map<String, String> result = memberService.confirmAuthCode(request);
        return ResponseEntity.ok(result);
    }
}
