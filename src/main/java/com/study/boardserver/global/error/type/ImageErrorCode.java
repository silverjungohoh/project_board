package com.study.boardserver.global.error.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ImageErrorCode {

    FAIL_TO_UPLOAD_IMAGE(HttpStatus.BAD_REQUEST, "이미지 업로드에 실패하였습니다"),
    INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 형식입니다."),
    EXCEEDED_IMAGE_SIZE_LIMIT(HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 이미지 크기를 초과하였습니다.");

    private final HttpStatus status;
    private final String message;
}
