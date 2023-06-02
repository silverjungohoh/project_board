package com.study.boardserver.global.error.exception;

import com.study.boardserver.global.error.type.ImageErrorCode;
import lombok.Getter;

@Getter
public class ImageException extends RuntimeException {

    private final ImageErrorCode errorCode;

    public ImageException(ImageErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
