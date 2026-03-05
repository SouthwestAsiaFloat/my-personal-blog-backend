package com.southwestasiafloat.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.entity.Category;

import java.util.Optional;

/**
 * Category 领域的服务接口，定义基础的 CRUD 能力。
 */
public interface CategoryService {
    IPage<Category> list(Page<Category> page);
    Optional<Category> getById(Long id);
    Category create(Category category);
    Category update(Long id, Category update);
    void delete(Long id);
}

