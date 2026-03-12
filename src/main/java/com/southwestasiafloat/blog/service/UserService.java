package com.southwestasiafloat.blog.service;

import com.southwestasiafloat.blog.dto.UserUpdateDto;
import com.southwestasiafloat.blog.vo.UserVo;

import java.util.Optional;

/**
 * 用户服务接口：仅处理用户资料域，认证能力迁移到 AuthService。
 */
public interface UserService {

    /** 根据 id 查找用户（返回 VO，避免暴露敏感字段）。 */
    Optional<UserVo> getById(Long id);

    /** 更新用户信息（PATCH 语义：仅更新传入字段）。 */
    UserVo update(Long id, UserUpdateDto update);
}
