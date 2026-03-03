package com.southwestasiafloat.blog.controller;

// 导入通用的 Result 返回类型（已放在 common 包）
import com.southwestasiafloat.blog.common.Result;
import com.southwestasiafloat.blog.entity.Article;
import com.southwestasiafloat.blog.repository.ArticleRepository;
import com.southwestasiafloat.blog.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("") // 使用根路径，应用的 context-path 已设为 /api
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    // 直接注入 repository 用于临时调试
    @Autowired
    private ArticleRepository articleRepository;

    @GetMapping("/article/{id}")
    public Result<Article> getArticle(@PathVariable("id") Long id) {
        Article a = articleService.getById(id).orElse(null);
        if (a == null) return Result.error(404, "not found");
        return Result.ok(a);
    }

    @GetMapping("/article/list")
    public Result<Page<Article>> getArticleList(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        Page<Article> p = articleService.list(PageRequest.of(page, size));
        return Result.ok(p);
    }

    @PostMapping("/article")
    public Result<Article> createArticle(@RequestBody Article article) {
        Article saved = articleService.create(article);
        return Result.ok(saved);
    }

    @PutMapping("/article/{id}")
    public Result<Article> updateArticle(@PathVariable("id") Long id, @RequestBody Article article) {
        Article updated = articleService.update(id, article);
        return Result.ok(updated);
    }

    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable("id") Long id) {
        articleService.delete(id);
        return Result.ok();
    }

    // 调试接口：直接使用 repository 查询并返回原始 Optional（临时使用，部署前应移除）
    @GetMapping("/debug/article/{id}")
    public Result<Optional<Article>> debugArticle(@PathVariable("id") Long id) {
        Optional<Article> opt = articleRepository.findById(id);
        if (opt.isEmpty()) return Result.error(404, "not found");
        return Result.ok(opt);
    }
}
