package com.study.boardserver.global.error.exception;

import com.study.boardserver.global.error.type.BoardErrorCode;
import lombok.Getter;

@Getter
public class BoardException extends RuntimeException{

    private final BoardErrorCode errorCode;

    public BoardException(BoardErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
