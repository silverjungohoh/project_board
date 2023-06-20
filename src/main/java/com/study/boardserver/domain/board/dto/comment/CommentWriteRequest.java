package com.study.boardserver.domain.board.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentWriteRequest {

    @NotNull(message = "댓글 내용을 입력하세요.")
    private String content;
}
