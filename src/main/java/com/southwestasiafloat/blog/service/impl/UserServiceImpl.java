package com.southwestasiafloat.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.southwestasiafloat.blog.entity.User;
import com.southwestasiafloat.blog.exception.AuthenticationException;
import com.southwestasiafloat.blog.mapper.UserMapper;
import com.southwestasiafloat.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

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
        // TODO: 验证密码（哈希比较）、生成 token 等
        throw new UnsupportedOperationException("login 未实现完整逻辑");
    }

    @Override
    public User register(User user) {
        // TODO: 验证唯一性、密码哈希
        userMapper.insert(user);
        return user;
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
