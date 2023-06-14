package com.study.boardserver.domain.board.dto.post;

import com.study.boardserver.domain.board.entity.PostImage;
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

    public static PostImageUrlResponse fromEntity(PostImage image) {
        return PostImageUrlResponse.builder()
                .imageId(image.getId())
                .imageUrl(image.getImgUrl())
                .build();
    }
}
