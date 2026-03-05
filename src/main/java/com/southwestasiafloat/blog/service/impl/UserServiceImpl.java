package com.southwestasiafloat.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.southwestasiafloat.blog.entity.User;
import com.southwestasiafloat.blog.exception.AuthenticationException;
import com.southwestasiafloat.blog.exception.ResourceNotFoundException;
import com.southwestasiafloat.blog.mapper.UserMapper;
import com.southwestasiafloat.blog.service.UserService;
import com.southwestasiafloat.blog.utils.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> getById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id));
    }

    @Override
    public void login(String username, String password) {
        // 根据用户输入的用户名查找用户
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            throw new AuthenticationException("用户不存在");
        }
        if (passwordEncoder.matches(password, user.getPassword())){
            // 登录成功，生成 token 或设置 session
            // TODO: 实现 JWT 或 Session 逻辑
            System.out.println("先假装登录成功了，后续实现 JWT 或 Session");
        } else {
            throw new AuthenticationException("密码错误");
        }
    }

    @Override
    @Transactional
    public User register(User user) {
        // 基本空值与格式校验
        if (user == null) throw new IllegalArgumentException("用户信息不能为空");
        String username = UserValidator.normalizeUsername(user.getUsername());
        String email = UserValidator.normalizeEmail(user.getEmail());
        String rawPassword = user.getPassword();

        UserValidator.validateUsernameOrThrow(username);
        UserValidator.validatePasswordOrThrow(rawPassword);
        UserValidator.validateEmailOrThrow(email);

        // 唯一性校验
        if (userMapper.selectCount(new QueryWrapper<User>().eq("username", username)) > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (userMapper.selectCount(new QueryWrapper<User>().eq("email", email)) > 0) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        // 哈希密码并设置默认字段
        String hashed = passwordEncoder.encode(rawPassword);
        user.setPassword(hashed);
        user.setUsername(username);
        user.setEmail(email);
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        if (user.getRole() == null) user.setRole("USER");

        userMapper.insert(user);
        // 返回数据库中的完整对象（包括自增 id）
        return userMapper.selectById(user.getId());
    }

    @Override
    @Transactional
    public User update(Long id, User update) {
        if (id == null || update == null) throw new IllegalArgumentException("用户或ID不能为空");
        User existing = userMapper.selectById(id);
        if (existing == null) throw new ResourceNotFoundException("用户不存在");

        String username = Optional.ofNullable(update.getUsername()).map(UserValidator::normalizeUsername).orElse(existing.getUsername());
        String email = Optional.ofNullable(update.getEmail()).map(UserValidator::normalizeEmail).orElse(existing.getEmail());
        UserValidator.validateUsernameOrThrow(username);
        UserValidator.validateEmailOrThrow(email);

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
            UserValidator.validatePasswordOrThrow(update.getPassword());
            existing.setPassword(passwordEncoder.encode(update.getPassword()));
        }
        existing.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(existing);
        existing.setPassword(null); // 避免返回哈希
        return existing;
    }

//    @Override
//    public Optional<User> getCurrentUser() {
//        // TODO: 根据上下文获取当前用户
//        return Optional.empty();
//    }
//
//    @Override
//    public void logout(String token) {
//        // TODO: 清理 server-side session 或 token 黑名单
//    }
//
//    @Override
//    public String refreshToken(String refreshToken) {
//        // TODO: 实现 JWT 刷新逻辑
//        throw new UnsupportedOperationException("refreshToken 未实现");
//    }
//
//    @Override
//    public void changePassword(Long userId, String oldPassword, String newPassword) {
//        // TODO: 验证旧密码并写入新密码（哈希）
//        throw new UnsupportedOperationException("changePassword 未实现");
//    }
//
//    @Override
//    public void requestPasswordReset(String email) {
//        // TODO: 发送邮件/验证码
//    }
//
//    @Override
//    public void resetPassword(String resetToken, String newPassword) {
//        // TODO: 使用 resetToken 重置密码
//        throw new UnsupportedOperationException("resetPassword 未实现");
//    }
//
//    @Override
//    public User updateProfile(Long userId, User update) {
//        User existing = userMapper.selectById(userId);
//        if (existing == null) throw new IllegalArgumentException("User not found");
//        // 只更新非 null 字段（简易实现）
//        if (update.getUsername() != null) existing.setUsername(update.getUsername());
//        if (update.getEmail() != null) existing.setEmail(update.getEmail());
//        userMapper.updateById(existing);
//        return existing;
//    }
//
//    @Override
//    public String uploadAvatar(Long userId, byte[] imageBytes, String filename) {
//        // TODO: 集成对象存储，返回 URL
//        throw new UnsupportedOperationException("uploadAvatar 未实现");
//    }
//
//    @Override
//    public IPage<User> listUsers(Page<User> page) {
//        return userMapper.selectPage(page, new QueryWrapper<>());
//    }
//
//    @Override
//    public boolean existsByUsername(String username) {
//        return userMapper.selectCount(new QueryWrapper<User>().eq("username", username)) > 0;
//    }
//
//    @Override
//    public boolean existsByEmail(String email) {
//        return userMapper.selectCount(new QueryWrapper<User>().eq("email", email)) > 0;
//    }
//
//    @Override
//    public void lockUser(Long userId) {
//        // TODO: 标记用户为锁定状态
//        throw new UnsupportedOperationException("lockUser 未实现");
//    }
//
//    @Override
//    public void unlockUser(Long userId) {
//        // TODO: 解锁用户
//        throw new UnsupportedOperationException("unlockUser 未实现");
//    }
//
//    @Override
//    public void softDeleteUser(Long userId) {
//        // 标记删除字段（若存在 isDeleted）
//        User u = userMapper.selectById(userId);
//        if (u != null) {
//            u.setIsDeleted(true);
//            userMapper.updateById(u);
//        }
//    }
//
//    @Override
//    public void hardDeleteUser(Long userId) {
//        userMapper.deleteById(userId);
//    }
//
//    @Override
//    public String[] getUserRoles(Long userId) {
//        // TODO: 若实现 RBAC，则从关联表查询
//        return new String[0];
//    }
//
//    @Override
//    public void assignRole(Long userId, String role) {
//        // TODO: 实现角色分配
//        throw new UnsupportedOperationException("assignRole 未实现");
//    }
//
//    @Override
//    public void revokeRole(Long userId, String role) {
//        // TODO: 实现角色撤销
//        throw new UnsupportedOperationException("revokeRole 未实现");
//    }

}
