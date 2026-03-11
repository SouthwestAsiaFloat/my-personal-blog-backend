package com.southwestasiafloat.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.dto.ArticleCreateDto;
import com.southwestasiafloat.blog.dto.ArticleUpdateDto;
import com.southwestasiafloat.blog.entity.Article;
import com.southwestasiafloat.blog.vo.ArticleVo;

import java.util.Optional;

public interface ArticleService {
    IPage<ArticleVo> list(Page<Article> page);
    Optional<ArticleVo> getById(Long id);
    ArticleVo create(ArticleCreateDto dto);
    ArticleVo update(Long id, ArticleUpdateDto dto);
    void delete(Long id);

    // 按状态、分类、标题模糊搜索分页查询（status: 1=发布, 0=草稿；传 null 则不过滤）
    IPage<ArticleVo> listBySearch(Page<Article> page, Integer status, Long categoryId, String title);
}
