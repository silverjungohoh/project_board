package com.study.boardserver.global.error.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode {

    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    FAIL_TO_SEND_EMAIL(HttpStatus.BAD_REQUEST, "이메일 전송에 실패하였습니다.");

    private final HttpStatus status;
    private final String message;
}
