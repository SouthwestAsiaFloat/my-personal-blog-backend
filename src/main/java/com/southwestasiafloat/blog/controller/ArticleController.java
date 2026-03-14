package com.southwestasiafloat.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.common.Result;
import com.southwestasiafloat.blog.dto.ArticleAssignTagsDto;
import com.southwestasiafloat.blog.dto.ArticleCreateDto;
import com.southwestasiafloat.blog.dto.ArticleUpdateDto;
import com.southwestasiafloat.blog.dto.ArticleRequestDto;
import com.southwestasiafloat.blog.entity.Article;
import com.southwestasiafloat.blog.entity.Tag;
import com.southwestasiafloat.blog.service.ArticleService;
import com.southwestasiafloat.blog.vo.ArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("") // 使用根路径，应用的 context-path 已设为 /api
@Slf4j
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    // 获取单个文章
    @GetMapping("/article/{id}")
    public Result<ArticleVo> getArticle(@PathVariable("id") Long id) {
        return articleService.getById(id)
                .map(Result::ok)
                .orElse(Result.error(404, "not found"));
    }


    // 获取非草稿文章列表，支持分页
//    @GetMapping("/article/list")
//    public Result<IPage<ArticleVo>> getArticleList(@RequestParam(defaultValue = "0") int page,
//                                                   @RequestParam(defaultValue = "10") int size) {
//        Page<Article> p = new Page<>(page + 1L, size);
//        return Result.ok(articleService.listByStatus(p, 1));
//    }
    // 获取全部文章列表,支持分页
    @PostMapping("/article/list")
    public Result<IPage<ArticleVo>> getArticleListAll(@RequestBody ArticleRequestDto dto)     {
        Page<Article> p = new Page<>(dto.getPage() + 1L, dto.getSize());
        Result<IPage<ArticleVo>> yourListresult = Result.ok(articleService.listBySearch(p, dto.getStatus(), dto.getCategoryId(), dto.getTitle()));
        if(yourListresult.getData().getRecords().isEmpty()){
            return Result.error(404, "文章未找到");
        }
        else {
            return yourListresult;
        }
    }
    // 获取草稿文章列表, 支持分页
//    @GetMapping("/article/list/draft")
//    public Result<IPage<ArticleVo>> getArticleListDraft(@RequestParam(defaultValue = "0") int page,
//                                                        @RequestParam(defaultValue = "10") int size) {
//        Page<Article> p = new Page<>(page + 1L, size);
//        return Result.ok(articleService.listByStatus(p, 0));
//    }
    /*    我是分隔栏     */
    // 创建文章
    @PostMapping("/article")
    public Result<ArticleVo> createArticle(@RequestBody ArticleCreateDto dto) {
        return Result.ok(articleService.create(dto));
    }

    // 更新文章
    @PutMapping("/article/{id}")
    public Result<ArticleVo> updateArticle(@PathVariable("id") Long id, @RequestBody ArticleUpdateDto dto) {
        return Result.ok(articleService.update(id, dto));
    }

    // 删除文章
    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable("id") Long id) {
        articleService.delete(id);
        return Result.ok();
    }

    // 给文章绑定一个或多个标签（覆盖式）
    @PutMapping("/article/{id}/tags")
    public Result<List<Long>> assignTags(@PathVariable("id") Long id, @RequestBody ArticleAssignTagsDto dto) {
        return Result.ok(articleService.assignTags(id, dto == null ? null : dto.getTagIds()));
    }

    // 获取某篇文章的全部标签
    @GetMapping("/article/{id}/tags")
    public Result<List<Tag>> listTagsByArticle(@PathVariable("id") Long id) {
        return Result.ok(articleService.listTagsByArticleId(id));
    }
}
