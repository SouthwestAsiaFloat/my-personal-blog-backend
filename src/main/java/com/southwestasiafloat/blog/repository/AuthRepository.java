package com.southwestasiafloat.blog.repository;

import com.southwestasiafloat.blog.entity.RefreshToken;
import com.southwestasiafloat.blog.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void insertUser(User user);

    Optional<RefreshToken> findRefreshTokenByHash(String tokenHash);

    void insertRefreshToken(RefreshToken token);

    void updateRefreshToken(RefreshToken token);

    /**
     * 仅当 token 处于有效状态（未撤销且未过期）时进行撤销，返回是否成功。
     */
    boolean revokeRefreshTokenIfActive(Long tokenId, LocalDateTime now, String replacedByHash);
}
