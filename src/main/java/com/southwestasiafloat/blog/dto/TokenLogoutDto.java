package com.southwestasiafloat.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 注销请求体 DTO（用于撤销 refresh token） */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenLogoutDto {
    private String refreshToken;
}

