package com.southwestasiafloat.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章与标签的中间表实体（多对多关系）。
 */
@TableName("article_tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleTag {

    // article_tag 当前表结构只有 article_id、tag_id 两列，没有自增 id
    @TableField("article_id")
    private Long articleId;

    @TableField("tag_id")
    private Long tagId;
}
