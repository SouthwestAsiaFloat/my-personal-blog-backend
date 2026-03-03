package com.southwestasiafloat.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.common.Result;
import com.southwestasiafloat.blog.entity.Article;
import com.southwestasiafloat.blog.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("") // 使用根路径，应用的 context-path 已设为 /api
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @GetMapping("/article/{id}")
    public Result<Article> getArticle(@PathVariable("id") Long id) {
        Article a = articleService.getById(id).orElse(null);
        if (a == null) return Result.error(404, "not found");
        return Result.ok(a);
    }

    @GetMapping("/article/list")
    public Result<IPage<Article>> getArticleList(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        Page<Article> p = new Page<>(page + 1L, size);
        return Result.ok(articleService.list(p));
    }

    @PostMapping("/article")
    public Result<Article> createArticle(@RequestBody Article article) {
        return Result.ok(articleService.create(article));
    }

    @PutMapping("/article/{id}")
    public Result<Article> updateArticle(@PathVariable("id") Long id, @RequestBody Article article) {
        return Result.ok(articleService.update(id, article));
    }

    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable("id") Long id) {
        articleService.delete(id);
        return Result.ok();
    }
}
