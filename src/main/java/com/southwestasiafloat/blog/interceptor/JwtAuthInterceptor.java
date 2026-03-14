package com.southwestasiafloat.blog.interceptor;

import com.southwestasiafloat.blog.common.Result;
import com.southwestasiafloat.blog.utils.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 基于 Access Token 的鉴权拦截器。
 *
 * 说明：
 * 1) 这里使用 Interceptor，不依赖 Spring Security Filter 链。
 * 2) 当前策略默认放行 GET（便于博客公开读取），仅拦截写操作。
 * 3) 鉴权成功后把 userId/role 放入 request attribute，便于后续业务读取。
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USER_ID = "authUserId";
    public static final String ATTR_ROLE = "authRole";

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public JwtAuthInterceptor(JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();

        // 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // 当前博客接口策略：读操作（GET）公开，写操作需要 token
        if ("GET".equalsIgnoreCase(method)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, "缺少或无效的 Authorization 头");
            return false;
        }

        String token = authorization.substring(7).trim();
        if (token.isEmpty()) {
            writeUnauthorized(response, "access token 不能为空");
            return false;
        }

        try {
            if (!jwtTokenProvider.validateToken(token)) {
                writeUnauthorized(response, "access token 无效或已过期");
                return false;
            }

            Long userId = jwtTokenProvider.getUserIdFromJWT(token);
            if (userId == null) {
                writeUnauthorized(response, "access token 中缺少用户信息");
                return false;
            }

            Claims claims = jwtTokenProvider.getClaimsFromToken(token);
            String role = claims.get("role", String.class);

            request.setAttribute(ATTR_USER_ID, userId);
            request.setAttribute(ATTR_ROLE, role);
            return true;
        } catch (Exception ex) {
            writeUnauthorized(response, "access token 验证失败");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, message)));
    }
}

