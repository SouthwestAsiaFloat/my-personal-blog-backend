package com.southwestasiafloat.blog.repository;

import com.southwestasiafloat.blog.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface
ArticleRepository extends JpaRepository<Article, Long> {
}
