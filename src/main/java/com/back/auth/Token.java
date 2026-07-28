package com.back.auth;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("Tokens")
public class Token {

    @Id
    private String id;

    @org.springframework.data.redis.core.index.Indexed
    private Long usuarioId;

    private String tipo;

    @TimeToLive
    private Long timeToLive;
}
