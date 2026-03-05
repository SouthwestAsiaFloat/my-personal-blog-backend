package com.southwestasiafloat.blog.utils;

import java.util.regex.Pattern;

/**
 * 用户相关的输入验证与规范化工具类。
 * - 提供 isValidXXX(boolean) / normalizeXXX / validateXXXOrThrow 方法，方便在 service/controller 中复用。
 */
public final class UserValidator {

    private UserValidator() {}

    // 简单的 email 验证（与项目中之前使用的 regex 保持一致）
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,6}$");
    // username 允许字母、数字、下划线和短横，长度 1..30（可根据需要调整）
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,30}$");

    /** 返回规范化后的 email（trim + toLowerCase），如果输入为 null 返回 null。 */
    public static String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    /** 返回规范化后的 username（trim），如果输入为 null 返回 null。 */
    public static String normalizeUsername(String username) {
        if (username == null) return null;
        return username.trim();
    }

    /** 简单密码合法性检查：非空且至少 6 位（保留原项目行为）。 */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static void validatePasswordOrThrow(String password) {
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("密码长度至少6位");
        }
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String trimmed = email.trim();
        return !trimmed.isEmpty() && EMAIL_PATTERN.matcher(trimmed).matches();
    }

    public static void validateEmailOrThrow(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("无效的邮箱格式");
        }
    }

    public static boolean isValidUsername(String username) {
        if (username == null) return false;
        String t = username.trim();
        return !t.isEmpty() && USERNAME_PATTERN.matcher(t).matches();
    }

    public static void validateUsernameOrThrow(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!isValidUsername(username)) {
            throw new IllegalArgumentException("用户名格式不合法，允许字母/数字/下划线/短横，长度 1-30");
        }
    }
}

