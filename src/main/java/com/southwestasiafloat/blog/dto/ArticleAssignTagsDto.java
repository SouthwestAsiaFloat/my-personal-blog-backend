package com.southwestasiafloat.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 给文章分配标签的请求体。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleAssignTagsDto {
    private List<Long> tagIds;
}

