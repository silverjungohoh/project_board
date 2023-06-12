package com.study.boardserver.domain.board.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImageUrlResponse {

    private Long imageId;

    private String imageUrl;
}
