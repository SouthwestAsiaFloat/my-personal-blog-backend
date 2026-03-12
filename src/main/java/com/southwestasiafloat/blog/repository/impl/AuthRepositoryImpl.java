package com.southwestasiafloat.blog.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.southwestasiafloat.blog.entity.RefreshToken;
import com.southwestasiafloat.blog.entity.User;
import com.southwestasiafloat.blog.mapper.RefreshTokenMapper;
import com.southwestasiafloat.blog.mapper.UserMapper;
import com.southwestasiafloat.blog.repository.AuthRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AuthRepositoryImpl implements AuthRepository {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;

    public AuthRepositoryImpl(UserMapper userMapper, RefreshTokenMapper refreshTokenMapper) {
        this.userMapper = userMapper;
        this.refreshTokenMapper = refreshTokenMapper;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userMapper.selectOne(new QueryWrapper<User>().eq("username", username)));
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.selectCount(new QueryWrapper<User>().eq("username", username)) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.selectCount(new QueryWrapper<User>().eq("email", email)) > 0;
    }

    @Override
    public void insertUser(User user) {
        userMapper.insert(user);
    }

    @Override
    public Optional<RefreshToken> findRefreshTokenByHash(String tokenHash) {
        return Optional.ofNullable(refreshTokenMapper.selectOne(
                new QueryWrapper<RefreshToken>().eq("token_hash", tokenHash).last("LIMIT 1")
        ));
    }

    @Override
    public void insertRefreshToken(RefreshToken token) {
        refreshTokenMapper.insert(token);
    }

    @Override
    public void updateRefreshToken(RefreshToken token) {
        refreshTokenMapper.updateById(token);
    }
}

