package com.southwestasiafloat.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.dto.ArticleCreateDto;
import com.southwestasiafloat.blog.dto.ArticleUpdateDto;
import com.southwestasiafloat.blog.entity.Article;
import com.southwestasiafloat.blog.mapper.ArticleMapper;
import com.southwestasiafloat.blog.service.ArticleService;
import com.southwestasiafloat.blog.vo.ArticleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public IPage<ArticleVo> list(Page<Article> page) {
        IPage<Article> entityPage = articleMapper.selectPage(page, new LambdaQueryWrapper<>());
        return entityPage.convert(this::toVo);
    }

    @Override
    public IPage<ArticleVo> listBySearch(Page<Article> page, Integer status, Long categoryId, String title) {
        LambdaQueryWrapper<Article> qw = new LambdaQueryWrapper<>();
        if (status != null) {
            qw.eq(Article::getStatus, status);
        }
        if (categoryId != null) {
            qw.eq(Article::getCategoryId, categoryId);
        }
        if (title != null && !title.trim().isEmpty()) {
            qw.like(Article::getTitle, title.trim());
        }
        IPage<Article> entityPage = articleMapper.selectPage(page, qw);
        return entityPage.convert(this::toVo);
    }

    @Override
    public Optional<ArticleVo> getById(Long id) {
        return Optional.ofNullable(articleMapper.selectById(id)).map(this::toVo);
    }

    @Override
    public ArticleVo create(ArticleCreateDto dto) {
        if (dto == null) throw new IllegalArgumentException("article body 不能为空");
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("title 不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        Article article = new Article();
        article.setTitle(dto.getTitle().trim());
        article.setContent(dto.getContent());
        article.setSummary(dto.getSummary());
        article.setUserId(dto.getUserId());
        article.setCategoryId(dto.getCategoryId());
        article.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        article.setIsDeleted(false);
        article.setCreateTime(now);
        article.setUpdateTime(now);

        articleMapper.insert(article);
        return toVo(article);
    }

    @Override
    public ArticleVo update(Long id, ArticleUpdateDto dto) {
        if (id == null) throw new IllegalArgumentException("id 不能为空");
        if (dto == null) throw new IllegalArgumentException("article body 不能为空");

        Article existing = Optional.ofNullable(articleMapper.selectById(id))
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        if (dto.getTitle() != null) {
            String t = dto.getTitle().trim();
            if (t.isEmpty()) throw new IllegalArgumentException("title 不能为空");
            existing.setTitle(t);
        }
        if (dto.getSummary() != null) existing.setSummary(dto.getSummary());
        if (dto.getContent() != null) existing.setContent(dto.getContent());
        if (dto.getUserId() != null) existing.setUserId(dto.getUserId());
        if (dto.getCategoryId() != null) existing.setCategoryId(dto.getCategoryId());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        existing.setUpdateTime(LocalDateTime.now());

        articleMapper.updateById(existing);
        return toVo(existing);
    }

    @Override
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("id 不能为空");
        articleMapper.deleteById(id);
    }

    private ArticleVo toVo(Article article) {
        return ArticleVo.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .summary(article.getSummary())
                .userId(article.getUserId())
                .categoryId(article.getCategoryId())
                .status(article.getStatus())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .build();
    }
}
