package com.southwestasiafloat.blog.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.southwestasiafloat.blog.common.Result;
import com.southwestasiafloat.blog.utils.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
            Claims claims = jwtTokenProvider.getClaimsFromToken(token);

            // refresh token 只能用于 /auth/refresh，不可当作访问令牌调用业务接口
            String type = claims.get("type", String.class);
            if ("refresh".equalsIgnoreCase(type)) {
                writeUnauthorized(response, "请使用 access token 访问该接口");
                return false;
            }

            Long userId = jwtTokenProvider.getUserIdFromJWT(token);
            if (userId == null) {
                writeUnauthorized(response, "access token 中缺少用户信息");
                return false;
            }

            String role = claims.get("role", String.class);
            request.setAttribute(ATTR_USER_ID, userId);
            request.setAttribute(ATTR_ROLE, role);
            return true;
        } catch (ExpiredJwtException ex) {
            writeUnauthorized(response, "access token 已过期");
            return false;
        } catch (JwtException | IllegalArgumentException ex) {
            writeUnauthorized(response, "access token 无效");
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

