package com.southwestasiafloat.blog.config;

import com.southwestasiafloat.blog.interceptor.JwtAuthInterceptor;
import com.southwestasiafloat.blog.interceptor.PublicReadOnlyInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 拦截器注册配置。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;

    public WebMvcConfig(JwtAuthInterceptor jwtAuthInterceptor) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PublicReadOnlyInterceptor())
                .addPathPatterns("/**");

        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                // 鉴权入口白名单：注册、登录、刷新、登出、错误页、健康检查
                .excludePathPatterns(
                        "/auth/register",
                        "/article/**", // 文章公开读取
                        "/comments/**", // 评论公开读取
                        "/auth/login",
                        "/auth/refresh",
                        "/auth/logout",
                        "/error",
                        "/actuator/**",
                        "/favicon.ico"
                );
    }
}
