package com.southwestasiafloat.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.entity.Article;
import java.util.Optional;

public interface ArticleService {
    IPage<Article> list(Page<Article> page);
    Optional<Article> getById(Long id);
    Article create(Article article);
    Article update(Long id, Article article);
    void delete(Long id);
}
