package com.popcoclient.redis.entity;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "blacklist", timeToLive = 60 * 60 * 12)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BlackList {
    @Id
    private String token; // accessToken or refreshToken
}
