package com.southwestasiafloat.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 用户登录请求 DTO */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginDto {
    private String username;
    private String password;
    private String token = null; // TODO: 可选字段，登录成功后返回的 JWT token
}

