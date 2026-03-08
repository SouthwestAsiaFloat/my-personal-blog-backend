package com.southwestasiafloat.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 创建文章请求 DTO */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleCreateDto {
    private String title;
    private String content;
    private String summary;
    private Long userId;
    private Long categoryId;
    private Integer status;
}

