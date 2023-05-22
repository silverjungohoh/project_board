package com.study.boardserver.domain.member.dto.signup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmAuthCodeRequest {

    private String email;

    private String code;
}
