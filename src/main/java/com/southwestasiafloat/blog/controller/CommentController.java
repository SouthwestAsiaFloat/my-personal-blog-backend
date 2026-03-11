package com.southwestasiafloat.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.common.Result;
import com.southwestasiafloat.blog.dto.CommentCreateDto;
import com.southwestasiafloat.blog.dto.CommentUpdateDto;
import com.southwestasiafloat.blog.entity.Comment;
import com.southwestasiafloat.blog.service.CommentService;
import com.southwestasiafloat.blog.vo.CommentVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@Slf4j
public class CommentController {

    @Autowired
    private CommentService commentService;


    @GetMapping
    public Result<IPage<CommentVo>> list(@RequestParam(required = false) Long articleId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) Long userId,
                                         @RequestParam(required = false) String nickname) {
        Page<Comment> p = new Page<>(page + 1L, size);
        Result<IPage<CommentVo>> result = new Result<>();
        try {
            IPage<CommentVo> commentPage = commentService.list(articleId, p, userId, nickname);
            if (commentPage.getRecords().isEmpty()) {
                return Result.error(404, "Comments not found");
            }
            result.setData(commentPage);
            result.setCode(200);
            result.setMessage("Success");
        } catch (Exception e) {
            log.error("Error listing comments", e);
            return Result.error(500, "Internal server error");
        }
        return result;
    }

    /** 根据 id 查询单条评论。 */
    @GetMapping("/{id}")
    public Result<CommentVo> getById(@PathVariable Long id) {
        return commentService.getById(id)
                .map(Result::ok)
                .orElse(Result.error(404, "Comment not found"));
    }

    /** 创建评论。 */
    @PostMapping
    public Result<CommentVo> create(@RequestBody CommentCreateDto dto) {
        try {
            return Result.ok(commentService.create(dto));
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 更新评论内容。 */
    @PutMapping("/{id}")
    public Result<CommentVo> update(@PathVariable Long id, @RequestBody CommentUpdateDto dto) {
        try {
            return Result.ok(commentService.update(id, dto));
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /** 删除评论。 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            commentService.delete(id);
            return Result.ok();
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }
}
