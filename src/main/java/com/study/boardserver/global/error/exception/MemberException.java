package com.study.boardserver.global.error.exception;

import com.study.boardserver.global.error.type.MemberErrorCode;
import lombok.Getter;

@Getter
public class MemberException extends RuntimeException{

    private final MemberErrorCode errorCode;

    public MemberException(MemberErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
