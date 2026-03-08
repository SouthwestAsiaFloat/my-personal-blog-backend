package com.southwestasiafloat.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.southwestasiafloat.blog.dto.UserLoginDto;
import com.southwestasiafloat.blog.dto.UserRegisterDto;
import com.southwestasiafloat.blog.dto.UserUpdateDto;
import com.southwestasiafloat.blog.entity.User;
import com.southwestasiafloat.blog.exception.AuthenticationException;
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
    public void login(UserLoginDto dto) {
        if (dto == null) throw new IllegalArgumentException("登录参数不能为空");
        String username = UserValidator.normalizeUsername(dto.getUsername());
        String password = dto.getPassword();
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("username and password are required");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            throw new AuthenticationException("用户不存在");
        }
        if (passwordEncoder.matches(password, user.getPassword())) {
            // TODO: 实现 JWT 或 Session 逻辑
            System.out.println("先假装登录成功了，后续实现 JWT 或 Session");
        } else {
            throw new AuthenticationException("密码错误");
        }
    }

    @Override
    @Transactional
    public UserVo register(UserRegisterDto dto) {
        if (dto == null) throw new IllegalArgumentException("用户信息不能为空");

        User toCreate = User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .build();

        // 统一输入格式校验
        UserValidator.validateUserForRegister(toCreate, false);
        String username = UserValidator.normalizeUsername(toCreate.getUsername());
        String email = UserValidator.normalizeEmail(toCreate.getEmail());

        if (userMapper.selectCount(new QueryWrapper<User>().eq("username", username)) > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (userMapper.selectCount(new QueryWrapper<User>().eq("email", email)) > 0) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        toCreate.setUsername(username);
        toCreate.setEmail(email);
        toCreate.setPassword(passwordEncoder.encode(toCreate.getPassword()));
        toCreate.setRole("USER");
        toCreate.setCreateTime(now);
        toCreate.setUpdateTime(now);

        userMapper.insert(toCreate);
        return toVo(userMapper.selectById(toCreate.getId()));
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
