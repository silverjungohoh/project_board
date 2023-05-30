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
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "refresh token이 유효하지 않습니다."),
    NOT_MATCH_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "refresh token이 일치하지 않습니다."),
    NOT_EXIST_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "refresh token이 존재하지 않습니다."),
    UNSUPPORTED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "지원되지 않는 access token 입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 access token 입니다."),
    INCORRECT_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 access token 입니다.");
    private final HttpStatus status;
    private final String message;
}
