package com.southwestasiafloat.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthVo {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn; // milliseconds
    private Long userId;
    private Boolean isAdmin;
}
