package com.southwestasiafloat.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.entity.Article;
import com.southwestasiafloat.blog.entity.Category;
import com.southwestasiafloat.blog.entity.Tag;
import com.southwestasiafloat.blog.mapper.ArticleMapper;
import com.southwestasiafloat.blog.mapper.CategoryMapper;
import com.southwestasiafloat.blog.mapper.TagMapper;
import com.southwestasiafloat.blog.service.TagService;
import com.southwestasiafloat.blog.vo.ArticleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public IPage<Tag> list(Page<Tag> page, String name) {
        LambdaQueryWrapper<Tag> qw = new LambdaQueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            qw.like(Tag::getName, name.trim());
        }
        return tagMapper.selectPage(page, qw);
    }

    @Override
    public Optional<Tag> getById(Long id) {
        return Optional.ofNullable(tagMapper.selectById(id));
    }

    @Override
    public Tag create(Tag tag) {
        if (tag == null) throw new IllegalArgumentException("tag不能为空");
        String name = tag.getName() == null ? null : tag.getName().trim();
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("标签名不能为空");

        long cnt = tagMapper.selectCount(new LambdaQueryWrapper<Tag>().eq(Tag::getName, name));
        if (cnt > 0) throw new IllegalArgumentException("标签名已存在");

        tag.setName(name);
        LocalDateTime now = LocalDateTime.now();
        if (tag.getCreateTime() == null) tag.setCreateTime(now);
        tag.setUpdateTime(now);
        tagMapper.insert(tag);
        return tag;
    }

    @Override
    public Tag update(Long id, Tag update) {
        Tag existing = tagMapper.selectById(id);
        if (existing == null) throw new IllegalArgumentException("Tag not found");

        if (update.getName() != null) {
            String name = update.getName().trim();
            if (name.isEmpty()) throw new IllegalArgumentException("标签名不能为空");
            long cnt = tagMapper.selectCount(new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getName, name)
                    .ne(Tag::getId, id));
            if (cnt > 0) throw new IllegalArgumentException("标签名已存在");
            existing.setName(name);
        }

        existing.setUpdateTime(LocalDateTime.now());
        tagMapper.updateById(existing);
        return existing;
    }

    @Override
    public void delete(Long id) {
        tagMapper.deleteById(id);
    }

    @Override
    public IPage<ArticleVo> listArticlesByTagId(Long tagId, Page<Article> page) {
        if (tagId == null) {
            throw new IllegalArgumentException("tagId 不能为空");
        }

        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new IllegalArgumentException("Tag not found");
        }

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.inSql(Article::getId, "SELECT article_id FROM article_tag WHERE tag_id = " + tagId)
                .eq(Article::getIsDeleted, false)
                .orderByDesc(Article::getCreateTime);

        IPage<Article> articlePage = articleMapper.selectPage(page, wrapper);

        List<Long> categoryIds = articlePage.getRecords().stream()
                .map(Article::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> categoryNameMap = Collections.emptyMap();
        if (!categoryIds.isEmpty()) {
            categoryNameMap = categoryMapper.selectBatchIds(categoryIds).stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
        }

        final Map<Long, String> finalCategoryNameMap = categoryNameMap;
        return articlePage.convert(article -> ArticleVo.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .summary(article.getSummary())
                .userId(article.getUserId())
                .categoryId(article.getCategoryId())
                .categoryName(finalCategoryNameMap.get(article.getCategoryId()))
                .status(article.getStatus())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .build());
    }
}
