package com.study.boardserver.global.error.exception;

import com.study.boardserver.global.error.type.MemberAuthErrorCode;
import lombok.Getter;

@Getter
public class MemberAuthException extends RuntimeException{

    private final MemberAuthErrorCode errorCode;

    public MemberAuthException(MemberAuthErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
