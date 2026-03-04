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
    // 获取单个文章
    @GetMapping("/article/{id}")
    public Result<Article> getArticle(@PathVariable("id") Long id) {
        Article a = articleService.getById(id).orElse(null);
        if (a == null) return Result.error(404, "not found");
        return Result.ok(a);
    }
    // 获取非草稿文章列表，支持分页
    @GetMapping("/article/list")
    public Result<IPage<Article>> getArticleList(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        Page<Article> p = new Page<>(page + 1L, size);
        return Result.ok(articleService.listByStatus(p, 1));
    }
    // 获取草稿文章列表, 支持分页
    @GetMapping("/article/list/draft")
    public Result<IPage<Article>> getArticleListDraft(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        Page<Article> p = new Page<>(page + 1L, size);
        // 在数据库层按 status=0 过滤，避免内存二次筛选带来的性能和分页问题
        return Result.ok(articleService.listByStatus(p, 0));
    }

    // 创建文章
    @PostMapping("/article")
    public Result<Article> createArticle(@RequestBody Article article) {
        return Result.ok(articleService.create(article));
    }
    // 更新文章
    @PutMapping("/article/{id}")
    public Result<Article> updateArticle(@PathVariable("id") Long id, @RequestBody Article article) {
        return Result.ok(articleService.update(id, article));
    }
    // 删除文章
    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable("id") Long id) {
        articleService.delete(id);
        return Result.ok();
    }
}
