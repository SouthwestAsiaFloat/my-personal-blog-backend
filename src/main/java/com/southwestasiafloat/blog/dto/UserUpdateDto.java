package com.southwestasiafloat.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 用户更新请求 DTO（PATCH 语义，字段可选） */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String role;
}

