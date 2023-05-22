package com.study.boardserver.domain.member.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;


@Getter
@Builder
@RedisHash("AuthCode")
public class MemberAuthCode {

    @Id
    @Indexed
    private String id;

    @Indexed
    private String email;

    @TimeToLive
    private Long expiredAt;
}
