package com.southwestasiafloat.blog.utils;

import com.southwestasiafloat.blog.interceptor.JwtAuthInterceptor;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 从拦截器写入的 request attribute 中读取当前登录态信息。
 */
public final class AuthContextUtil {

    private AuthContextUtil() {
    }

    public static Long getCurrentUserId(HttpServletRequest request) {
        Object value = request.getAttribute(JwtAuthInterceptor.ATTR_USER_ID);
        if (value instanceof Long userId) {
            return userId;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    public static String getCurrentRole(HttpServletRequest request) {
        Object value = request.getAttribute(JwtAuthInterceptor.ATTR_ROLE);
        return value == null ? null : String.valueOf(value);
    }

    public static boolean isAdmin(HttpServletRequest request) {
        String role = getCurrentRole(request);
        return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
    }

    public static boolean isSelfOrAdmin(HttpServletRequest request, Long targetUserId) {
        Long currentUserId = getCurrentUserId(request);
        return isAdmin(request) || (currentUserId != null && currentUserId.equals(targetUserId));
    }
}
