package com.southwestasiafloat.blog.utils;

import cn.hutool.json.JSONUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

// 封装的Redis工具类
@Component
public class CacheClient {

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(8);

    private final StringRedisTemplate redisTemplate;

    public CacheClient(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 将任意Java对象序列化为JSON字符串并存储到Redis中
    public void set(String key, Object value, Long time, TimeUnit unit) {
        String json = JSONUtil.toJsonStr(value);
        redisTemplate.opsForValue().set(key, json, time, unit);
    }

    // 将Java对象以逻辑过期的方式存储到Redis中（Redis键不设置真实TTL）
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(System.currentTimeMillis() + unit.toMillis(time));
        String json = JSONUtil.toJsonStr(redisData);
        redisTemplate.opsForValue().set(key, json);
    }

    // 从Redis中查询数据，并将JSON字符串反序列化为Java对象
    public <R> R get(String key, Class<R> type) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        return JSONUtil.toBean(json, type);
    }

    // 读取逻辑过期结构；即便过期也返回旧值（是否重建由上层方法处理）
    public <R> R getWithLogicalExpire(String key, Class<R> type) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isEmpty()) {
            return null;
        }
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        return JSONUtil.toBean(JSONUtil.toJsonStr(redisData.getData()), type);
    }

    // 防止缓存穿透的查询方法
    public <R, ID> R queryWithPassThrough(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = redisTemplate.opsForValue().get(key);
        if (json != null) {
            if (json.isEmpty()) {
                // 命中空值，返回null
                return null;
            }
            // 命中正常值，返回反序列化的对象
            return JSONUtil.toBean(json, type);
        }
        // 若缓存未命中，回源数据库
        R result = dbFallback.apply(id);
        if (result == null) {
            // 数据库也没有，缓存空值，防止缓存穿透
            redisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 数据库有值，缓存到Redis
        this.set(key, result, time, unit);
        return result;
    }

    public boolean tryLock(String key) {
        return tryLockWithToken(key) != null;
    }

    public String tryLockWithToken(String key) {
        String token = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, token, RedisConstants.LOCK_BLOG_TTL, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success) ? token : null;
    }

    public boolean unlock(String key) {
        return redisTemplate.delete(key);
    }

    public boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    // 仅当锁值匹配时才删除，避免误删其他线程持有的锁
    public boolean unlockSafely(String key, String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
        script.setResultType(Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(key), token);
        return result != null && result > 0;
    }

    // 互斥锁解决缓存击穿的查询方法
    public <R, ID> R queryWithMutex(
            String keyPrefix,
            String lockKeyPrefix,
            ID id,
            Class<R> type,
            Function<ID, R> dbFallback,
            Long time,
            TimeUnit unit) {
        String key = keyPrefix + id;
        String json = redisTemplate.opsForValue().get(key);
        if (json != null) {
            if (json.isEmpty()) {
                // 命中空值，返回null
                return null;
            }
            // 命中正常值，返回反序列化的对象
            return JSONUtil.toBean(json, type);
        }

        String lockKey = lockKeyPrefix + id;
        String lockToken = tryLockWithToken(lockKey);
        if (lockToken == null) {
            // 获取锁失败，休眠并重试
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return queryWithMutex(keyPrefix, lockKeyPrefix, id, type, dbFallback, time, unit);
        }

        try {
            // 获取锁成功，再次检查缓存
            json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                if (json.isEmpty()) {
                    return null;
                }
                return JSONUtil.toBean(json, type);
            }
            // 缓存仍然没有，查询数据库
            R result = dbFallback.apply(id);
            if (result == null) {
                redisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            this.set(key, result, time, unit);
            return result;
        } finally {
            // 释放锁（只释放自己持有的锁）
            unlockSafely(lockKey, lockToken);
        }
    }

    // 逻辑过期解决缓存击穿的查询方法
    public <R, ID> R queryWithLogicalExpire(
            String keyPrefix,
            String lockKeyPrefix,
            ID id,
            Class<R> type,
            Function<ID, R> dbFallback,
            Long time,
            TimeUnit unit) {
        String key = keyPrefix + id;
        String json = redisTemplate.opsForValue().get(key);

        // 冷启动：缓存不存在时回源数据库并写入逻辑过期结构
        if (json == null || json.isEmpty()) {
            R dbResult = dbFallback.apply(id);
            if (dbResult == null) {
                redisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            setWithLogicalExpire(key, dbResult, time, unit);
            return dbResult;
        }

        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R result = JSONUtil.toBean(JSONUtil.toJsonStr(redisData.getData()), type);

        // 未过期，直接返回
        if (redisData.getExpireTime() >= System.currentTimeMillis()) {
            return result;
        }

        // 已过期：返回旧值，同时尝试异步重建
        String lockKey = lockKeyPrefix + id;
        String lockToken = tryLockWithToken(lockKey);
        if (lockToken != null) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    R dbResult = dbFallback.apply(id);
                    if (dbResult == null) {
                        redisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                        return;
                    }
                    setWithLogicalExpire(key, dbResult, time, unit);
                } finally {
                    unlockSafely(lockKey, lockToken);
                }
            });
        }

        return result;
    }
}
