package com.southwestasiafloat.blog.service.impl;

import com.southwestasiafloat.blog.entity.Article;
import com.southwestasiafloat.blog.repository.ArticleRepository;
import com.southwestasiafloat.blog.service.ArticleService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Override
    public Page<Article> list(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    @Override
    public Optional<Article> getById(Long id) {
        return articleRepository.findById(id);
    }

    @Override
    public Article create(Article article) {
        return articleRepository.save(article);
    }

    @Override
    public Article update(Long id, Article article) {
        return articleRepository.findById(id).map(existing -> {
            existing.setTitle(article.getTitle());
            existing.setContent(article.getContent());
            existing.setSummary(article.getSummary());
            existing.setStatus(article.getStatus());
            return articleRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Article not found"));
    }

    @Override
    public void delete(Long id) {
        articleRepository.deleteById(id);
    }
}
