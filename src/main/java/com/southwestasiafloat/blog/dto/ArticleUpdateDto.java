package com.southwestasiafloat.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 更新文章请求 DTO（PUT 语义，允许覆盖可编辑字段） */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleUpdateDto {
    private String title;
    private String content;
    private String summary;
    private Long userId;
    private Long categoryId;
    private Integer status;
}

