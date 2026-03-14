package com.southwestasiafloat.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.entity.Article;
import com.southwestasiafloat.blog.entity.Tag;
import com.southwestasiafloat.blog.vo.ArticleVo;

import java.util.Optional;

public interface TagService {
    IPage<Tag> list(Page<Tag> page, String name);
    Optional<Tag> getById(Long id);
    Tag create(Tag tag);
    Tag update(Long id, Tag update);
    void delete(Long id);

    /** 根据标签分页查询对应文章列表。 */
    IPage<ArticleVo> listArticlesByTagId(Long tagId, Page<Article> page);
}
