package com.study.boardserver.global.error.handler;

import com.study.boardserver.global.error.exception.BoardException;
import com.study.boardserver.global.error.exception.ImageException;
import com.study.boardserver.global.error.exception.MemberAuthException;
import com.study.boardserver.global.error.exception.MemberException;
import com.study.boardserver.global.error.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Objects;

import static com.study.boardserver.global.error.type.ImageErrorCode.EXCEEDED_IMAGE_SIZE_LIMIT;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(Objects.requireNonNull(e.getFieldError()).getDefaultMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MemberAuthException.class)
    public ResponseEntity<ErrorResponse> handleMemberAuthException(MemberAuthException e) {

        ErrorResponse response = ErrorResponse.builder()
                .status(e.getErrorCode().getStatus().value())
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    @ExceptionHandler(ImageException.class)
    public ResponseEntity<ErrorResponse> handleImageException(ImageException e) {

        ErrorResponse response = ErrorResponse.builder()
                .status(e.getErrorCode().getStatus().value())
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException() {

        ErrorResponse response = ErrorResponse.builder()
                .status(EXCEEDED_IMAGE_SIZE_LIMIT.getStatus().value())
                .message(EXCEEDED_IMAGE_SIZE_LIMIT.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(BoardException.class)
    public ResponseEntity<ErrorResponse> handleBoardException(BoardException e) {

        ErrorResponse response = ErrorResponse.builder()
                .status(e.getErrorCode().getStatus().value())
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }
}
