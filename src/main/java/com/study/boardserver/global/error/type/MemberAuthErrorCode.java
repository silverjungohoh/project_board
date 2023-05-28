package com.study.boardserver.global.error.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberAuthErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    FAIL_TO_AUTHENTICATION(HttpStatus.UNAUTHORIZED, "사용자 인증에 실패하였습니다."),
    FAIL_TO_AUTHORIZATION(HttpStatus.FORBIDDEN, "사용자 권한이 없습니다."),
    NOT_MATCH_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "refresh token이 일치하지 않습니다."),
    ALREADY_EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "refresh token이 만료되었습니다.");
    private final HttpStatus status;
    private final String message;
}
