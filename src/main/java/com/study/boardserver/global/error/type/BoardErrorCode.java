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
    CANNOT_UPDATE_POST(HttpStatus.FORBIDDEN, "게시물 수정 권한은 작성자에게 있습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    CANNOT_UPDATE_COMMENT(HttpStatus.FORBIDDEN, "댓글 수정 권한은 작성자에게 있습니다."),
    CANNOT_DELETE_COMMENT(HttpStatus.FORBIDDEN, "댓글 삭제 권한은 작성자에게 있습니다."),
    CANNOT_PUSH_HEART(HttpStatus.BAD_REQUEST, "자신의 게시물에 좋아요를 누를 수 없습니다."),
    ALREADY_PUSH_HEART(HttpStatus.BAD_REQUEST, "이미 게시물에 좋아요를 눌렀습니다.");

    private final HttpStatus status;
    private final String message;
}
