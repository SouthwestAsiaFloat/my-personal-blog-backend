package com.southwestasiafloat.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRequestDto {
    private int page = 0; // 默认页码
    private int size = 10; // 默认每页条数
    private String title; // 可选的标题模糊搜索
    private Long categoryId; // 可选的分类过滤
    private Integer status = null;  // 可选的状态过滤，默认只获取非草稿文章（1），0 表示草稿，null 表示全部
}
