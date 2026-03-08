package com.southwestasiafloat.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 文章响应 VO */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleVo {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private Long userId;
    private Long categoryId;
    private int status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

