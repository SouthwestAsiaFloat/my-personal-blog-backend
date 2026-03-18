package com.southwestasiafloat.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.dto.ArticleCreateDto;
import com.southwestasiafloat.blog.dto.ArticleUpdateDto;
import com.southwestasiafloat.blog.entity.Article;
import com.southwestasiafloat.blog.entity.ArticleTag;
import com.southwestasiafloat.blog.entity.Category;
import com.southwestasiafloat.blog.entity.Tag;
import com.southwestasiafloat.blog.mapper.ArticleMapper;
import com.southwestasiafloat.blog.mapper.ArticleTagMapper;
import com.southwestasiafloat.blog.mapper.CategoryMapper;
import com.southwestasiafloat.blog.mapper.TagMapper;
import com.southwestasiafloat.blog.service.ArticleService;
import com.southwestasiafloat.blog.utils.CacheClient;
import com.southwestasiafloat.blog.utils.RedisConstants;
import com.southwestasiafloat.blog.vo.ArticleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private CacheClient cacheClient;

    @Override
    public IPage<ArticleVo> list(Page<Article> page) {
        IPage<Article> entityPage = articleMapper.selectPage(page, new LambdaQueryWrapper<>());
        // 批量加载 category name
        return convertPageWithCategory(entityPage);
    }

    @Override
    public IPage<ArticleVo> listBySearch(Page<Article> page, Integer status, Long categoryId, String title) {
        LambdaQueryWrapper<Article> qw = new LambdaQueryWrapper<>();
        if (status != null) {
            qw.eq(Article::getStatus, status);
        }
        if (categoryId != null) {
            qw.eq(Article::getCategoryId, categoryId);
        }
        if (title != null && !title.trim().isEmpty()) {
            qw.like(Article::getTitle, title.trim());
        }
        IPage<Article> entityPage = articleMapper.selectPage(page, qw);
        return convertPageWithCategory(entityPage);
    }

    @Override
    public Optional<ArticleVo> getById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        ArticleVo cachedOrDb = cacheClient.queryWithPassThrough(
                RedisConstants.CACHE_ARTICLE_DETAIL_KEY,
                id,
                ArticleVo.class,
                this::loadArticleVoById,
                RedisConstants.CACHE_ARTICLE_DETAIL_TTL,
                TimeUnit.MINUTES
        );
        return Optional.ofNullable(cachedOrDb);
    }

    @Override
    public ArticleVo create(ArticleCreateDto dto) {
        if (dto == null) throw new IllegalArgumentException("article body 不能为空");
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("title 不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        Article article = new Article();
        article.setTitle(dto.getTitle().trim());
        article.setContent(dto.getContent());
        article.setSummary(dto.getSummary());
        article.setUserId(dto.getUserId());
        article.setCategoryId(dto.getCategoryId());
        article.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        article.setIsDeleted(false);
        article.setCreateTime(now);
        article.setUpdateTime(now);

        articleMapper.insert(article);
        ArticleVo vo = toVo(article);
        if (vo.getCategoryId() != null) {
            Category c = categoryMapper.selectById(vo.getCategoryId());
            if (c != null) vo.setCategoryName(c.getName());
        }
        return vo;
    }

    @Override
    public ArticleVo update(Long id, ArticleUpdateDto dto) {
        if (id == null) throw new IllegalArgumentException("id 不能为空");
        if (dto == null) throw new IllegalArgumentException("article body 不能为空");

        Article existing = Optional.ofNullable(articleMapper.selectById(id))
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        if (dto.getTitle() != null) {
            String t = dto.getTitle().trim();
            if (t.isEmpty()) throw new IllegalArgumentException("title 不能为空");
            existing.setTitle(t);
        }
        if (dto.getSummary() != null) existing.setSummary(dto.getSummary());
        if (dto.getContent() != null) existing.setContent(dto.getContent());
        if (dto.getUserId() != null) existing.setUserId(dto.getUserId());
        if (dto.getCategoryId() != null) existing.setCategoryId(dto.getCategoryId());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        existing.setUpdateTime(LocalDateTime.now());

        articleMapper.updateById(existing);
        evictArticleDetailCache(id);

        ArticleVo vo = toVo(existing);
        if (vo.getCategoryId() != null) {
            Category c = categoryMapper.selectById(vo.getCategoryId());
            if (c != null) vo.setCategoryName(c.getName());
        }
        return vo;
    }

    @Override
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("id 不能为空");
        articleMapper.deleteById(id);
        evictArticleDetailCache(id);
    }

    @Override
    @Transactional
    public List<Long> assignTags(Long articleId, List<Long> tagIds) {
        if (articleId == null) {
            throw new IllegalArgumentException("articleId 不能为空");
        }

        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new IllegalArgumentException("Article not found");
        }

        if (tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("至少需要一个 tagId");
        }

        // 去重 + 过滤非法 id，确保一次请求可绑定 1..N 个有效标签
        List<Long> normalizedTagIds = tagIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .collect(Collectors.toList());

        if (normalizedTagIds.isEmpty()) {
            throw new IllegalArgumentException("tagIds 不合法");
        }

        List<Tag> existingTags = tagMapper.selectBatchIds(normalizedTagIds);
        Set<Long> existingTagIds = existingTags.stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());

        if (existingTagIds.size() != normalizedTagIds.size()) {
            List<Long> missingTagIds = normalizedTagIds.stream()
                    .filter(id -> !existingTagIds.contains(id))
                    .collect(Collectors.toList());
            throw new IllegalArgumentException("以下标签不存在: " + missingTagIds);
        }

        // 覆盖式绑定：先删除旧关系，再插入新关系
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, articleId));

        for (Long tagId : normalizedTagIds) {
            articleTagMapper.insert(ArticleTag.builder()
                    .articleId(articleId)
                    .tagId(tagId)
                    .build());
        }

        // 更新文章更新时间，便于审计“最近修改”
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(article);
        evictArticleDetailCache(articleId);

        return normalizedTagIds;
    }

    @Override
    public List<Tag> listTagsByArticleId(Long articleId) {
        if (articleId == null) throw new IllegalArgumentException("articleId 不能为空");

        Article article = articleMapper.selectById(articleId);
        if (article == null) throw new IllegalArgumentException("Article not found");

        // 通过 article_tag 找 tag_id 列表
        List<ArticleTag> relations = articleTagMapper.selectList(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, articleId));

        if (relations.isEmpty()) return Collections.emptyList();

        List<Long> tagIds = relations.stream().map(ArticleTag::getTagId).distinct().collect(Collectors.toList());
        if (tagIds.isEmpty()) return Collections.emptyList();

        return tagMapper.selectBatchIds(tagIds);
    }

    private ArticleVo loadArticleVoById(Long id) {
        Article a = articleMapper.selectById(id);
        if (a == null) {
            return null;
        }

        ArticleVo vo = toVo(a);
        if (vo.getCategoryId() != null) {
            Category c = categoryMapper.selectById(vo.getCategoryId());
            if (c != null) {
                vo.setCategoryName(c.getName());
            }
        }
        return vo;
    }

    private void evictArticleDetailCache(Long articleId) {
        if (articleId == null) {
            return;
        }
        cacheClient.delete(RedisConstants.CACHE_ARTICLE_DETAIL_KEY + articleId);
    }

    private ArticleVo toVo(Article article) {
        return ArticleVo.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .summary(article.getSummary())
                .userId(article.getUserId())
                .categoryId(article.getCategoryId())
                .status(article.getStatus())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .build();
    }

    private IPage<ArticleVo> convertPageWithCategory(IPage<Article> entityPage) {
        // 收集 categoryId
        List<Long> categoryIds = entityPage.getRecords().stream()
                .map(Article::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> categoryNameMap = Collections.emptyMap();
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
            categoryNameMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
        }

        final Map<Long, String> finalCategoryNameMap = categoryNameMap;
        return entityPage.convert(article -> {
            ArticleVo vo = toVo(article);
            vo.setCategoryName(finalCategoryNameMap.get(article.getCategoryId()));
            return vo;
        });
    }
}
