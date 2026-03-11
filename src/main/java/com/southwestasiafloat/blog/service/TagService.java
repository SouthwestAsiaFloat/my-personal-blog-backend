package com.southwestasiafloat.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southwestasiafloat.blog.entity.Tag;

import java.util.Optional;

public interface TagService {
    IPage<Tag> list(Page<Tag> page, String name);
    Optional<Tag> getById(Long id);
    Tag create(Tag tag);
    Tag update(Long id, Tag update);
    void delete(Long id);
}
