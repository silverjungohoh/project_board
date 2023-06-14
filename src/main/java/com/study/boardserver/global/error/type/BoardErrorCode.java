package com.study.boardserver.global.error.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BoardErrorCode {

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시물입니다."),
    CANNOT_DELETE_POST(HttpStatus.FORBIDDEN, "게시물 삭제 권한은 작성자에게 있습니다."),
    POST_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 이미지입니다."),
    CANNOT_UPDATE_POST(HttpStatus.FORBIDDEN, "게시물 수정 권한은 작성자에게 있습니다.");

    private final HttpStatus status;
    private final String message;
}
