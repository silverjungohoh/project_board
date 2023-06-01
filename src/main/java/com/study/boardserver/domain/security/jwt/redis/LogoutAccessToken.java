package com.study.boardserver.domain.security.jwt.redis;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;


@Getter
@Builder
@RedisHash("logoutAccessToken")
public class LogoutAccessToken {

    @Id
    private String id;

    private String email;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private Long expiration;
}
