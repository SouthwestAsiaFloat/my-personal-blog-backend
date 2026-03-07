package com.southwestasiafloat.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.dto.CommentCreateDto;
import com.southwestasiafloat.blog.dto.CommentUpdateDto;
import com.southwestasiafloat.blog.entity.Comment;
import com.southwestasiafloat.blog.mapper.CommentMapper;
import com.southwestasiafloat.blog.service.CommentService;
import com.southwestasiafloat.blog.vo.CommentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public IPage<CommentVo> list(Long articleId, Page<Comment> page) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .orderByDesc(Comment::getCreateTime);
        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }

        IPage<Comment> entityPage = commentMapper.selectPage(page, wrapper);
        // 将 Entity 分页结果转换为 VO 分页结果
        return entityPage.convert(this::toVo);
    }

    @Override
    public Optional<CommentVo> getById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(commentMapper.selectById(id)).map(this::toVo);
    }

    @Override
    public CommentVo create(CommentCreateDto dto) {
        if (dto == null) throw new IllegalArgumentException("评论数据不能为空");
        if (dto.getArticleId() == null) throw new IllegalArgumentException("articleId 不能为空");
        if (dto.getUserId() == null) throw new IllegalArgumentException("userId 不能为空");

        String content = normalizeContent(dto.getContent());
        if (content == null || content.isEmpty()) throw new IllegalArgumentException("评论内容不能为空");

        LocalDateTime now = LocalDateTime.now();
        Comment comment = Comment.builder()
                .articleId(dto.getArticleId())
                .userId(dto.getUserId())
                .content(content)
                .createTime(now)
                .updateTime(now)
                .build();

        commentMapper.insert(comment);
        return toVo(comment);
    }

    @Override
    public CommentVo update(Long id, CommentUpdateDto dto) {
        if (id == null) throw new IllegalArgumentException("id 不能为空");
        if (dto == null) throw new IllegalArgumentException("更新数据不能为空");

        Comment existing = commentMapper.selectById(id);
        if (existing == null) throw new IllegalArgumentException("评论不存在");

        String content = normalizeContent(dto.getContent());
        if (content == null || content.isEmpty()) throw new IllegalArgumentException("评论内容不能为空");

        existing.setContent(content);
        existing.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(existing);
        return toVo(existing);
    }

    @Override
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("id 不能为空");
        Comment existing = commentMapper.selectById(id);
        if (existing == null) throw new IllegalArgumentException("评论不存在");
        commentMapper.deleteById(id);
    }

    private String normalizeContent(String content) {
        if (content == null) return null;
        String normalized = content.trim();
        // 避免评论无限长导致存储或渲染问题
        if (normalized.length() > 2000) {
            throw new IllegalArgumentException("评论内容长度不能超过 2000");
        }
        return normalized;
    }

    private CommentVo toVo(Comment comment) {
        return CommentVo.builder()
                .id(comment.getId())
                .articleId(comment.getArticleId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .updateTime(comment.getUpdateTime())
                .build();
    }
}
