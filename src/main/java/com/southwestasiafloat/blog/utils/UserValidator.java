package com.southwestasiafloat.blog.utils;

import com.southwestasiafloat.blog.entity.User;

import java.util.regex.Pattern;
import java.util.Objects;

/**
 * 用户相关的输入验证与规范化工具类。
 * - 提供 isValidXXX / normalizeXXX / validateXXXOrThrow 方法，方便在 service/controller 中复用。
 */
public final class UserValidator {

    private UserValidator() {}

    // 简单的 email 验证（与项目中之前使用的 regex 保持一致）
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,6}$");
    // username 允许字母、数字、下划线和短横，长度 1..30（可根据需要调整）
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,30}$");
    // 国际手机号简单验证：可选 + 开头，7 到 15 位数字
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{7,15}$");
    // 强密码（推荐）：至少8位，包含小写、大写和数字
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

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

    /** 推荐的强密码验证：至少8位，包含大小写和数字。 */
    public static boolean isStrongPassword(String password) {
        if (password == null) return false;
        return STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }

    public static void validateStrongPasswordOrThrow(String password) {
        if (!isStrongPassword(password)) {
            throw new IllegalArgumentException("密码至少8位，且包含大小写字母和数字");
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

    // ----- 额外字段验证 -----

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static void validatePhoneOrThrow(String phone) {
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException("无效的手机号格式");
        }
    }

    public static boolean isValidNickname(String nickname) {
        if (nickname == null) return true; // 昵称可选
        String t = nickname.trim();
        return t.length() <= 50;
    }

    public static void validateNicknameOrThrow(String nickname) {
        if (!isValidNickname(nickname)) {
            throw new IllegalArgumentException("昵称长度不能超过50个字符");
        }
    }

    // ----- 高阶便捷方法：用于在 Service/Controller 中一次性校验 User 对象 -----

    /**
     * 验证用于注册的用户对象（会检查 username/email/password 是否存在并合法）。
     * @param user 要验证的用户
     * @throws IllegalArgumentException 校验失败时抛出
     */
    public static void validateUserForRegister(User user) {
        validateUserForRegister(user, false);
    }

    /**
     * 验证用于注册的用户对象，允许选择是否要求强密码。
     * @param user 要验证的用户
     * @param requireStrongPassword 是否要求强密码策略
     */
    public static void validateUserForRegister(User user, boolean requireStrongPassword) {
        if (user == null) throw new IllegalArgumentException("用户信息不能为空");
        String username = normalizeUsername(user.getUsername());
        String email = normalizeEmail(user.getEmail());
        String rawPassword = user.getPassword();

        if (username == null || username.isEmpty()) throw new IllegalArgumentException("用户名不能为空");
        if (email == null || email.isEmpty()) throw new IllegalArgumentException("邮箱不能为空");
        if (rawPassword == null || rawPassword.isEmpty()) throw new IllegalArgumentException("密码不能为空");

        validateUsernameOrThrow(username);
        if (requireStrongPassword) validateStrongPasswordOrThrow(rawPassword);
        else validatePasswordOrThrow(rawPassword);
        validateEmailOrThrow(email);

        // 可选字段
        if (user.getNickname() != null) validateNicknameOrThrow(user.getNickname());
        if (user.getRole() != null) {
            // 简单限制：角色长度不超过 50
            if (user.getRole().length() > 50) throw new IllegalArgumentException("角色字段过长");
        }
    }

    /**
     * 验证用于更新的用户对象（只检查非 null 的字段是否合法）。
     * @param update 更新对象（可能只包含部分字段）
     * @param requireStrongPassword 是否要求密码满足强密码策略（当 password 非 null 时）
     */
    public static void validateUserForUpdate(User update, boolean requireStrongPassword) {
        if (update == null) throw new IllegalArgumentException("更新对象不能为空");
        if (update.getUsername() != null) validateUsernameOrThrow(normalizeUsername(update.getUsername()));
        if (update.getEmail() != null) validateEmailOrThrow(normalizeEmail(update.getEmail()));
        if (update.getPassword() != null) {
            if (requireStrongPassword) validateStrongPasswordOrThrow(update.getPassword());
            else validatePasswordOrThrow(update.getPassword());
        }
        if (update.getNickname() != null) validateNicknameOrThrow(update.getNickname());
        if (update.getRole() != null && update.getRole().length() > 50) throw new IllegalArgumentException("角色字段过长");
    }
}
