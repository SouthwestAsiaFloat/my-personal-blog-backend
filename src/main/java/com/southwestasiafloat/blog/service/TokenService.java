package com.southwestasiafloat.blog.service;

import com.southwestasiafloat.blog.entity.RefreshToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenService {

    void saveRefreshToken(RefreshToken token);

    Optional<RefreshToken> findRefreshTokenByHash(String tokenHash);

    void revokeRefreshToken(String tokenHash, LocalDateTime revokedAt, String replacedByHash);

    void deleteRefreshToken(String tokenHash);
}

