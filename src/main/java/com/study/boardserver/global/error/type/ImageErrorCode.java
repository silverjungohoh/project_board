package com.study.boardserver.global.error.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ImageErrorCode {

    FAIL_TO_UPLOAD_IMAGE(HttpStatus.BAD_REQUEST, "이미지 업로드에 실패하였습니다"),
    INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 형식입니다.");

    private final HttpStatus status;
    private final String message;
}
