package com.southwestasiafloat.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.common.Result;
import com.southwestasiafloat.blog.entity.Category;
import com.southwestasiafloat.blog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public Result<IPage<Category>> list(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        Page<Category> p = new Page<>(page + 1L, size);
        return Result.ok(categoryService.list(p));
    }

    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        return categoryService.getById(id)
                .map(Result::ok)
                .orElse(Result.error(404, "Category not found"));
    }

    @PostMapping
    public Result<Category> create(@RequestBody Category category) {
        return Result.ok(categoryService.create(category));
    }

    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Long id, @RequestBody Category category) {
        return Result.ok(categoryService.update(id, category));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {

        if(!categoryService.getById(id).isPresent()
        ){
                return Result.error(404, "Category not found");
        }
        categoryService.delete(id);
        return Result.ok();
    }
}

