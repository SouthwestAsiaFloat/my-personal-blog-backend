package com.southwestasiafloat.blog.utils;

import lombok.Data;

@Data
public class RedisData {
    private Object data; // 存储的实际数据
    private Long expireTime; // 逻辑过期时间，单位毫秒
}
