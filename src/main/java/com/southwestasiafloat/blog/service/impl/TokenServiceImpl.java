package com.southwestasiafloat.blog.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.southwestasiafloat.blog.entity.RefreshToken;
import com.southwestasiafloat.blog.service.TokenService;
import com.southwestasiafloat.blog.utils.RedisConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TokenServiceImpl implements TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenServiceImpl.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public TokenServiceImpl(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveRefreshToken(RefreshToken token) {
        if (token == null || token.getTokenHash() == null || token.getTokenHash().trim().isEmpty()) {
            return;
        }
        String hash = token.getTokenHash().trim();
        String key = key(hash);
        try {
            long ttlSeconds = resolveTtlSeconds(token.getExpiresAt());
            if (ttlSeconds <= 0) {
                deleteSilentlyByKey(key, hash);
                return;
            }
            String json = objectMapper.writeValueAsString(token);
            redisTemplate.opsForValue().set(key, json, Duration.ofSeconds(ttlSeconds));
        } catch (JsonProcessingException e) {
            log.warn("serialize refresh token failed, hash={}", hash, e);
            deleteSilentlyByKey(key, hash);
        } catch (Exception e) {
            log.warn("save refresh token to redis failed, hash={}", hash, e);
        }
    }

    @Override
    public Optional<RefreshToken> findRefreshTokenByHash(String tokenHash) {
        if (tokenHash == null || tokenHash.trim().isEmpty()) {
            return Optional.empty();
        }

        String hash = tokenHash.trim();
        String key = key(hash);
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isEmpty()) {
                return Optional.empty();
            }

            RefreshToken token = objectMapper.readValue(json, RefreshToken.class);
            if (token == null || token.getTokenHash() == null || token.getTokenHash().trim().isEmpty()) {
                log.warn("invalid refresh token cache payload, hash={}", hash);
                deleteSilentlyByKey(key, hash);
                return Optional.empty();
            }

            if (!hash.equals(token.getTokenHash().trim())) {
                log.warn("refresh token hash mismatch in cache, expected={}, actual={}", hash, token.getTokenHash());
                deleteSilentlyByKey(key, hash);
                return Optional.empty();
            }

            if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
                deleteSilentlyByKey(key, hash);
                return Optional.empty();
            }

            return Optional.of(token);
        } catch (Exception e) {
            log.warn("load refresh token from redis failed, hash={}", hash, e);
            deleteSilentlyByKey(key, hash);
            return Optional.empty();
        }
    }

    @Override
    public void revokeRefreshToken(String tokenHash, LocalDateTime revokedAt, String replacedByHash) {
        Optional<RefreshToken> existing = findRefreshTokenByHash(tokenHash);
        if (existing.isEmpty()) {
            return;
        }

        RefreshToken token = existing.get();
        if (token.getTokenHash() == null || token.getTokenHash().trim().isEmpty()) {
            token.setTokenHash(tokenHash);
        }
        token.setRevoked(true);
        token.setRevokedAt(revokedAt == null ? LocalDateTime.now() : revokedAt);
        token.setReplacedBy(replacedByHash);
        saveRefreshToken(token);
    }

    @Override
    public void deleteRefreshToken(String tokenHash) {
        if (tokenHash == null || tokenHash.trim().isEmpty()) {
            return;
        }
        deleteSilentlyByKey(key(tokenHash.trim()), tokenHash.trim());
    }

    private String key(String tokenHash) {
        return RedisConstants.REFRESH_TOKEN_KEY + tokenHash;
    }

    private long resolveTtlSeconds(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            return 0L;
        }
        Duration duration = Duration.between(LocalDateTime.now(), expiresAt);
        return Math.max(duration.getSeconds(), 0L);
    }

    private void deleteSilentlyByKey(String key, String tokenHash) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("delete refresh token in redis failed, hash={}", tokenHash, e);
        }
    }
}
