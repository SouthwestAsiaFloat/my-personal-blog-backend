package com.southwestasiafloat.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于创建评论的 DTO
 * 注意：项目当前未引入校验实现（hibernate-validator），如需启用 @Valid 请添加 spring-boot-starter-validation 依赖。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentCreateDto {

    // 文章 ID，必填
    private Long articleId;

    // 用户 ID，必填
    private Long userId;

    // 评论内容，必填
    private String content;
}
