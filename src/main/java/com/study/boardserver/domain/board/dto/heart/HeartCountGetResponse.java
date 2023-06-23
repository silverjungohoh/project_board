package com.study.boardserver.domain.board.dto.heart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeartCountGetResponse {

    private Long postId;

    private Long heartCnt;
}
