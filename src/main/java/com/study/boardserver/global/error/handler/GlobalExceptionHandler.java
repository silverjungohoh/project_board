package com.study.boardserver.global.error.handler;

import com.study.boardserver.global.error.exception.MemberException;
import com.study.boardserver.global.error.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ErrorResponse> handleMemberException(MemberException e) {

        ErrorResponse response = ErrorResponse.builder()
                .status(e.getErrorCode().getStatus().value())
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }
}
