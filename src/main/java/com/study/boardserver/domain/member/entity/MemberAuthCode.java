package com.study.boardserver.domain.member.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;


@Getter
@Builder
@RedisHash("AuthCode")
public class MemberAuthCode {

    @Id
    private String id;

    private String email;

    @TimeToLive
    private Long expiredAt;
}
