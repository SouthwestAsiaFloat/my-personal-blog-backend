package com.southwestasiafloat.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.entity.User;
import java.util.Optional;

/**
 * 用户服务接口：定义个人博客中常见的用户相关操作
 */
public interface UserService {
    /** 根据 id 查找用户 */
    Optional<User> getById(Long id);

    /**
     * 登录：校验凭证并返回登录成功后的上下文（可在实现中返回 JWT/Session 信息或抛出异常）
     */
    void login(String username, String password);

    /** 注册新用户并返回创建的用户对象（注意：实现应处理密码哈希、唯一性校验等） */
    User register(User user);

    // --------- 建议的额外方法（常见功能） ---------

//    /** 获取当前登录用户（若未登录可返回 Optional.empty()） */
//    Optional<User> getCurrentUser();
//
//    /** 登出（清理服务器端 session 或在客户端删除 token） */
//    void logout(String token);
//
//    /**
//     * 刷新 token（若使用 JWT 刷新机制）
//     * 实现可以返回新的 token 或在异常情况下抛出异常
//     */
//    String refreshToken(String refreshToken);
//
//    /** 修改密码：需验证旧密码并写入新密码（实现中请哈希新密码） */
//    void changePassword(Long userId, String oldPassword, String newPassword);
//
//    /** 发起重置密码请求（发送邮件或验证码） */
//    void requestPasswordReset(String email);
//
//    /** 使用重置令牌/验证码真正重置密码 */
//    void resetPassword(String resetToken, String newPassword);
//
//    /** 更新用户资料（不包含密码，头像等可单独接口） */
//    User updateProfile(Long userId, User update);
//
//    /** 上传/更新用户头像，返回最终的头像 URL（实现可接入对象存储） */
//    String uploadAvatar(Long userId, byte[] imageBytes, String filename);
//
//    /** 分页查询用户（管理员场景） */
//    IPage<User> listUsers(Page<User> page);
//
//    /** 根据 username 或 email 判断是否存在（用于注册校验） */
//    boolean existsByUsername(String username);
//    boolean existsByEmail(String email);
//
//    /** 锁定 / 解锁 用户（管理员操作） */
//    void lockUser(Long userId);
//    void unlockUser(Long userId);
//
//    /** 软删除用户（标记 isDeleted）或彻底删除 */
//    void softDeleteUser(Long userId);
//    void hardDeleteUser(Long userId);
//
//    /** 查询用户的角色或权限（若实现 RBAC） */
//    String[] getUserRoles(Long userId);
//
//    /** 给用户分配角色（管理员） */
//    void assignRole(Long userId, String role);
//
//    /** 撤销角色 */
//    void revokeRole(Long userId, String role);

}
