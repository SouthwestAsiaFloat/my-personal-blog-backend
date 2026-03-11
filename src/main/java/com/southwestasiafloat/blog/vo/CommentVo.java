package com.southwestasiafloat.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentVo {
    private Long id;
    private Long articleId;
    private Long userId;
    private String nickname;
    private String articleTitle; // 新增：文章标题
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
