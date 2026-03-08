package com.southwestasiafloat.blog.service;

import com.southwestasiafloat.blog.dto.UserLoginDto;
import com.southwestasiafloat.blog.dto.UserRegisterDto;
import com.southwestasiafloat.blog.dto.UserUpdateDto;
import com.southwestasiafloat.blog.vo.UserVo;
import com.southwestasiafloat.blog.vo.AuthVo;

import java.util.Optional;

/**
 * 用户服务接口：使用 DTO/VO 与外部层交互，内部由 Entity 持久化。
 */
public interface UserService {

    /** 根据 id 查找用户（返回 VO，避免暴露敏感字段）。 */
    Optional<UserVo> getById(Long id);

    /** 登录：校验凭证并返回认证令牌（access + refresh）。 */
    AuthVo login(UserLoginDto dto) throws Exception;

    /** 注册新用户并返回创建后的用户视图。 */
    UserVo register(UserRegisterDto dto);

    /** 更新用户信息（PATCH 语义：仅更新传入字段）。 */
    UserVo update(Long id, UserUpdateDto update);
}
