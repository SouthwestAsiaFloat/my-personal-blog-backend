package com.southwestasiafloat.blog.utils;

public class RedisConstants {

    public static final String LOGIN_CODE_KEY = "login:code";
    public static final Long LOGIN_CODE_TTL = 2L; // 验证码过期时间，单位分钟
    public static final String LOGIN_USER_KEY = "login:token";
    public static final Long LOGIN_USER_TTL = 36000L; // 登录状态过期时间，单位秒

    public static final Long CACHE_NULL_TTL = 2L; // 缓存空值的过期时间，单位分钟

    public static final String CACHE_BLOG_KEY = "cache:blog:";
    public static final Long CACHE_BLOG_TTL = 30L; // 博客数据
    public static final String LOCK_BLOG_KEY = "lock:blog:";
    public static final Long LOCK_BLOG_TTL = 10L; // 锁的

    public static final String REFRESH_TOKEN_KEY = "auth:refresh:";

    public static final String CACHE_ARTICLE_DETAIL_KEY = "cache:article:detail:";
    public static final Long CACHE_ARTICLE_DETAIL_TTL = 30L; // 文章详情缓存，单位分钟
}
