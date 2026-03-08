package com.southwestasiafloat.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 用户注册请求 DTO */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterDto {
    private String username;
    private String password;
    private String nickname;
    private String email;
}

