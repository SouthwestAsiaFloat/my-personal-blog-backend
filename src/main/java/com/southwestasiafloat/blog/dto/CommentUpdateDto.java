package com.southwestasiafloat.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 编辑评论的 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentUpdateDto {

    // 评论内容，必填
    private String content;
}
