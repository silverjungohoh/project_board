package com.study.boardserver.global.error.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode {

    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    FAIL_TO_SEND_EMAIL(HttpStatus.BAD_REQUEST, "이메일 전송에 실패하였습니다."),
    INVALID_EMAIL_AUTH_CODE(HttpStatus.BAD_REQUEST, "이메일 인증 코드가 유효하지 않습니다."),
    NOT_FINISH_EMAIL_AUTH(HttpStatus.BAD_REQUEST, "이메일 인증을 완료해주세요.");

    private final HttpStatus status;
    private final String message;
}
