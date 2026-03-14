package com.southwestasiafloat.blog.controller;

import com.southwestasiafloat.blog.common.Result;
import com.southwestasiafloat.blog.dto.TokenLogoutDto;
import com.southwestasiafloat.blog.dto.TokenRefreshDto;
import com.southwestasiafloat.blog.dto.UserLoginDto;
import com.southwestasiafloat.blog.dto.UserRegisterDto;
import com.southwestasiafloat.blog.service.AuthService;
import com.southwestasiafloat.blog.vo.AuthVo;
import com.southwestasiafloat.blog.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({ "/register"})
    public Result<UserVo> register(@RequestBody UserRegisterDto dto) {
        return Result.ok(authService.register(dto));
    }

    @PostMapping({ "/login"})
    public Result<AuthVo> login(@RequestBody UserLoginDto dto, HttpServletRequest request) throws Exception {
        // 覆盖请求体中的 ip/ua，统一使用服务端拿到的来源信息，避免客户端伪造
        dto.setIp(resolveClientIp(request));
        dto.setUserAgent(request.getHeader("User-Agent"));
        return Result.ok(authService.login(dto));
    }

    @PostMapping({ "/refresh"})
    public Result<AuthVo> refresh(@RequestBody TokenRefreshDto dto, HttpServletRequest request) throws Exception {
        String ip = resolveClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        return Result.ok(authService.refresh(dto.getRefreshToken(), ip, userAgent));
    }

    @PostMapping({ "/logout"})
    public Result<Void> logout(@RequestBody TokenLogoutDto dto) throws Exception {
        authService.logout(dto.getRefreshToken());
        return Result.ok(null);
    }

    private String resolveClientIp(HttpServletRequest request) {
        // 常见反向代理头优先；本地开发通常会回落到 remoteAddr
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            return forwarded.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}
