package com.southwestasiafloat.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.dto.CommentCreateDto;
import com.southwestasiafloat.blog.dto.CommentUpdateDto;
import com.southwestasiafloat.blog.entity.Comment;
import com.southwestasiafloat.blog.vo.CommentVo;

import java.util.Optional;

/**
 * Comment 领域服务：定义评论的基础 CRUD 能力。
 */
public interface CommentService {

    /**
     * 分页查询评论。
     * articleId 非空时按文章过滤，为 null 时查询全部评论。
     */
    IPage<CommentVo> list(Long articleId, Page<Comment> page);

    /** 根据评论 ID 查询单条评论。 */
    Optional<CommentVo> getById(Long id);

    /** 创建评论。 */
    CommentVo create(CommentCreateDto dto);

    /** 更新评论（当前只更新 content）。 */
    CommentVo update(Long id, CommentUpdateDto dto);

    /** 删除评论。 */
    void delete(Long id);
}

