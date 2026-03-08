package com.southwestasiafloat.blog.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 工具类
 * 负责生成和验证 JWT token，提取用户信息等功能
 * 支持 Access Token 和 Refresh Token 的生成和验证
 */
@Component
public class JwtTokenProvider {

    // Jwt密钥，建议在 production 中通过环境变量注入并且长度足够（>=32 bytes）
    @Value("${jwt.secret:ChangeThisJwtSecretChangeThisJwtSecret}")
    private String jwtSecret;

    // Access Token过期时间（毫秒）, 默认 30 分钟
    @Value("${jwt.accessExpirationMs:1800000}")
    private Long accessTokenValidityInMillis;

    // Refresh Token过期时间（毫秒）, 默认 7 天
    @Value("${jwt.refreshExpirationMs:604800000}")
    private Long refreshTokenValidityInMillis;

    // 内部签名 Key
    private Key getSigningKey() {
        byte[] keyBytes = null;
        if (jwtSecret == null) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        // Try to interpret secret as Base64 first (common when generating random bytes)
        try {
            byte[] decoded = Base64.getDecoder().decode(jwtSecret);
            if (decoded != null && decoded.length >= 32) {
                keyBytes = decoded;
            }
        } catch (IllegalArgumentException ignored) {
            // not valid base64, fall back to UTF-8 bytes
        }
        if (keyBytes == null) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 Access Token（JWT）
     * 包含 subject=userId, claim: role, exp
     */
    public String generateAccessToken(Long userId, String role) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiryDate = new Date(now + accessTokenValidityInMillis);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(issuedAt)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成 Refresh Token（也使用 JWT 结构以便自包含过期信息）
     * refresh token 可以只包含 userId，也可以加入 tokenVersion 或 jti
     */
    public String generateRefreshToken(Long userId) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiryDate = new Date(now + refreshTokenValidityInMillis);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("type", "refresh")
                .setIssuedAt(issuedAt)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证 token 是否有效（签名与过期）
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // 包括签名异常、过期、格式错误等
            return false;
        }
    }

    /**
     * 从 token 中提取用户 ID（subject）
     */
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
        String sub = claims.getSubject();
        if (sub == null) return null;
        try {
            return Long.parseLong(sub);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从 token 中获取 Claims（如果需要额外信息）
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    // 公共 getter，供其他 service 使用过期时长
    public Long getAccessTokenValidityInMillis() {
        return accessTokenValidityInMillis;
    }

    public Long getRefreshTokenValidityInMillis() {
        return refreshTokenValidityInMillis;
    }
}
