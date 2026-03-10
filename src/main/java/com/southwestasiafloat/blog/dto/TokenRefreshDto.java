package com.southwestasiafloat.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshDto {
    private String refreshToken;
    private String ip; // 客户端 IP
    private String userAgent; // 客户端 UA


}