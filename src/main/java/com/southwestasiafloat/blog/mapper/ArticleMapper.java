package com.southwestasiafloat.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.southwestasiafloat.blog.entity.Article;

// 使用主类上的 @MapperScan 扫描 Mapper，因此这里不需要 @Mapper 注解
public interface ArticleMapper extends BaseMapper<Article> {
}
