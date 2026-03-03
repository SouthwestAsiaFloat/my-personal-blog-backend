package com.southwestasiafloat.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.entity.Article;
import com.southwestasiafloat.blog.mapper.ArticleMapper;
import com.southwestasiafloat.blog.service.ArticleService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    // 文章列表，分页查询
    @Override
    public IPage<Article> list(Page<Article> page) {
        return articleMapper.selectPage(page, new LambdaQueryWrapper<>());
    }

    // 单独查询
    @Override
    public Optional<Article> getById(Long id) {
        return Optional.ofNullable(articleMapper.selectById(id));
    }


    // 创建文章
    @Override
    public Article create(Article article) {
        articleMapper.insert(article);
        return article;
    }

    // 更新文章
    @Override
    public Article update(Long id, Article article) {
        return Optional.ofNullable(articleMapper.selectById(id)).map(existing -> {
            existing.setTitle(article.getTitle());
            existing.setSummary(article.getSummary());
            existing.setContent(article.getContent());
            existing.setUserId(article.getUserId());
            existing.setCategoryId(article.getCategoryId());
            existing.setStatus(article.getStatus());
            existing.setIsDeleted(article.getIsDeleted());
            articleMapper.updateById(existing);
            return existing;
        }).orElseThrow(() -> new IllegalArgumentException("Article not found"));
    }

    @Override
    public void delete(Long id) {
        articleMapper.deleteById(id);
    }
}
