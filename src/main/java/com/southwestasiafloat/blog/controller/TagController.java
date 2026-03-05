package com.southwestasiafloat.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.common.Result;
import com.southwestasiafloat.blog.entity.Tag;
import com.southwestasiafloat.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    public Result<IPage<Tag>> list(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        Page<Tag> p = new Page<>(page + 1L, size);
        return Result.ok(tagService.list(p));
    }

    @GetMapping("/{id}")
    public Result<Tag> getById(@PathVariable Long id) {
        return tagService.getById(id)
                .map(Result::ok)
                .orElse(Result.error(404, "Tag not found"));
    }

    @PostMapping
    public Result<Tag> create(@RequestBody Tag tag) {
        try {
            return Result.ok(tagService.create(tag));
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Tag> update(@PathVariable Long id, @RequestBody Tag tag) {
        try {
            return Result.ok(tagService.update(id, tag));
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            tagService.delete(id);
            return Result.ok();
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }
}

