package com.southwestasiafloat.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 用户响应 VO（不返回 password） */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVo {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String role;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

