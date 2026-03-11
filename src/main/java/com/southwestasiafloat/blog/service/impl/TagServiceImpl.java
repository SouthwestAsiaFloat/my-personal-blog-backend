package com.southwestasiafloat.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.entity.Tag;
import com.southwestasiafloat.blog.mapper.TagMapper;
import com.southwestasiafloat.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

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
}

