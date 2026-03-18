package com.southwestasiafloat.blog.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

/**
 * 对公开资源做最小访问控制：GET 放行，写操作要求携带 Authorization 头。
 */
public class PublicReadOnlyInterceptor implements HandlerInterceptor {

    // 公开可读资源前缀
    private final List<String> publicPrefixes = List.of("/article", "/comments");

    private boolean isPublicPath(String path) {
        for (String p : publicPrefixes) {
            if (path.equals(p) || path.startsWith(p + "/")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (isPublicPath(path)) {
            if ("GET".equalsIgnoreCase(method)) {
                // 公开读取允许匿名访问
                return true;
            }
            // 非 GET 写操作必须带 Authorization，具体 token 校验交给 JwtAuthInterceptor
            String auth = request.getHeader("Authorization");
            if (auth == null || auth.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("authentication required for write operations");
                return false;
            }
        }
        return true;
    }
}
