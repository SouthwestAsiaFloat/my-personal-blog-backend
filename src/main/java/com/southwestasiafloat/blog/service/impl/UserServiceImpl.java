package com.southwestasiafloat.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.southwestasiafloat.blog.dto.UserUpdateDto;
import com.southwestasiafloat.blog.entity.User;
import com.southwestasiafloat.blog.exception.ResourceNotFoundException;
import com.southwestasiafloat.blog.mapper.UserMapper;
import com.southwestasiafloat.blog.service.UserService;
import com.southwestasiafloat.blog.utils.UserValidator;
import com.southwestasiafloat.blog.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Optional<UserVo> getById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id)).map(this::toVo);
    }

    @Override
    @Transactional
    public UserVo update(Long id, UserUpdateDto update) {
        if (id == null || update == null) throw new IllegalArgumentException("用户或ID不能为空");
        User existing = userMapper.selectById(id);
        if (existing == null) throw new ResourceNotFoundException("用户不存在");

        String username = update.getUsername() != null
                ? UserValidator.normalizeUsername(update.getUsername())
                : existing.getUsername();
        String email = update.getEmail() != null
                ? UserValidator.normalizeEmail(update.getEmail())
                : existing.getEmail();

        UserValidator.validateUsernameOrThrow(username);
        UserValidator.validateEmailOrThrow(email);
        if (update.getPassword() != null && !update.getPassword().isEmpty()) {
            UserValidator.validatePasswordOrThrow(update.getPassword());
        }

        if (userMapper.selectCount(new QueryWrapper<User>().eq("username", username).ne("id", id)) > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (userMapper.selectCount(new QueryWrapper<User>().eq("email", email).ne("id", id)) > 0) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        existing.setUsername(username);
        existing.setEmail(email);
        if (update.getNickname() != null) existing.setNickname(update.getNickname());
        if (update.getRole() != null) existing.setRole(update.getRole());
        if (update.getPassword() != null && !update.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(update.getPassword()));
        }
        existing.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(existing);
        return toVo(existing);
    }

    private UserVo toVo(User user) {
        if (user == null) return null;
        return UserVo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .role(user.getRole())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }
}
