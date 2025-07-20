package com.popcoclient.redis.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "token", timeToLive = 60 * 60 * 12) // 12시간 유지
@AllArgsConstructor
@Getter
@ToString
public class Token {
    @Id
    private Long id;
    private String refreshToken;
}