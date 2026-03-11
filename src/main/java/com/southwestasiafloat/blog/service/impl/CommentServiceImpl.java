package com.southwestasiafloat.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.dto.CommentCreateDto;
import com.southwestasiafloat.blog.dto.CommentUpdateDto;
import com.southwestasiafloat.blog.entity.Comment;
import com.southwestasiafloat.blog.entity.User;
import com.southwestasiafloat.blog.entity.Article;
import com.southwestasiafloat.blog.mapper.CommentMapper;
import com.southwestasiafloat.blog.mapper.UserMapper;
import com.southwestasiafloat.blog.mapper.ArticleMapper;
import com.southwestasiafloat.blog.service.CommentService;
import com.southwestasiafloat.blog.vo.CommentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public IPage<CommentVo> list(Long articleId, Page<Comment> page, Long userId, String nickname) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .orderByDesc(Comment::getCreateTime);
        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }

        // 优先使用 userId（精确匹配），否则如果给了 nickname 则根据 nickname 模糊查询 user 表拿到 id 列表
        if (userId != null) {
            wrapper.eq(Comment::getUserId, userId);
        } else if (StringUtils.hasText(nickname)) {
            LambdaQueryWrapper<User> uw = new LambdaQueryWrapper<User>()
                    .like(User::getNickname, nickname)
                    .select(User::getId);
            List<User> users = userMapper.selectList(uw);
            if (users == null || users.isEmpty()) {
                Page<CommentVo> emptyVo = new Page<>(page.getCurrent(), page.getSize(), 0);
                emptyVo.setRecords(Collections.emptyList());
                return emptyVo;
            }
            List<Long> ids = users.stream().map(User::getId).collect(Collectors.toList());
            wrapper.in(Comment::getUserId, ids);
        }

        IPage<Comment> entityPage = commentMapper.selectPage(page, wrapper);

        // 批量查询昵称，避免分页结果逐条 selectById 造成 N+1
        Set<Long> userIds = entityPage.getRecords().stream()
                .map(Comment::getUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> nicknameMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            nicknameMap = users.stream()
                    .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));
        }

        // 批量查询文章标题，避免 N+1
        Set<Long> articleIds = entityPage.getRecords().stream()
                .map(Comment::getArticleId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> articleTitleMap = Collections.emptyMap();
        if (!articleIds.isEmpty()) {
            List<Article> articles = articleMapper.selectBatchIds(articleIds);
            articleTitleMap = articles.stream()
                    .collect(Collectors.toMap(Article::getId, Article::getTitle, (a, b) -> a));
        }

        final Map<Long, String> finalNicknameMap = nicknameMap;
        final Map<Long, String> finalArticleTitleMap = articleTitleMap;
        return entityPage.convert(comment -> toVo(comment,
                finalNicknameMap.get(comment.getUserId()),
                finalArticleTitleMap.get(comment.getArticleId())));
    }

    @Override
    public Optional<CommentVo> getById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(commentMapper.selectById(id))
                .map(comment -> toVo(comment,
                        resolveNickname(comment.getUserId()),
                        resolveArticleTitle(comment.getArticleId())));
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
        return toVo(comment,
                resolveNickname(comment.getUserId()),
                resolveArticleTitle(comment.getArticleId()));
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
        return toVo(existing,
                resolveNickname(existing.getUserId()),
                resolveArticleTitle(existing.getArticleId()));
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

    private CommentVo toVo(Comment comment, String nickname, String articleTitle) {
        return CommentVo.builder()
                .id(comment.getId())
                .articleId(comment.getArticleId())
                .userId(comment.getUserId())
                .nickname(nickname)
                .articleTitle(articleTitle)
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .updateTime(comment.getUpdateTime())
                .build();
    }

    private String resolveNickname(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        return user == null ? null : user.getNickname();
    }

    private String resolveArticleTitle(Long articleId) {
        if (articleId == null) return null;
        Article article = articleMapper.selectById(articleId);
        return article == null ? null : article.getTitle();
    }
}
