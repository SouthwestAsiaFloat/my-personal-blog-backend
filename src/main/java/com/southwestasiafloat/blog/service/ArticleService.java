package com.southwestasiafloat.blog.service;

import com.southwestasiafloat.blog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ArticleService {
    Page<Article> list(Pageable pageable);
    Optional<Article> getById(Long id);
    Article create(Article article);
    Article update(Long id, Article article);
    void delete(Long id);
}
