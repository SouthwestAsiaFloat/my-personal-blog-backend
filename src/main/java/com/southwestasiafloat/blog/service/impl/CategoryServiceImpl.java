package com.southwestasiafloat.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.entity.Category;
import com.southwestasiafloat.blog.mapper.CategoryMapper;
import com.southwestasiafloat.blog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public IPage<Category> list(Page<Category> page) {
        return categoryMapper.selectPage(page, null);
    }

    @Override
    public Optional<Category> getById(Long id) {
        return Optional.ofNullable(categoryMapper.selectById(id));
    }

    @Override
    public Category create(Category category) {
        // 基本校验
        if (category == null) throw new IllegalArgumentException("category cannot be null");
        String name = category.getName();
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("分类名称不能为空");

        // 唯一性校验（数据库中是否已存在相同名称）
        long cnt = categoryMapper.selectCount(new LambdaQueryWrapper<Category>().eq(Category::getName, name));
        if (cnt > 0) {
            throw new IllegalArgumentException("已有相同名称的分类");
        }

        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category update(Long id, Category update) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Category not found");
        }
        if (update.getName() != null) existing.setName(update.getName());
        if (update.getCreateTime() != null) existing.setCreateTime(update.getCreateTime());
        if (update.getUpdateTime() != null) existing.setUpdateTime(update.getUpdateTime());
        categoryMapper.updateById(existing);
        return existing;
    }

    @Override
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        if ( categoryMapper.selectById(id) == null) throw new IllegalArgumentException("Category not found");
        categoryMapper.deleteById(id);
    }
}
