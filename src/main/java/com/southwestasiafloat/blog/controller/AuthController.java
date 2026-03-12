package com.southwestasiafloat.blog.controller;

import com.southwestasiafloat.blog.common.Result;
import com.southwestasiafloat.blog.dto.TokenLogoutDto;
import com.southwestasiafloat.blog.dto.TokenRefreshDto;
import com.southwestasiafloat.blog.dto.UserLoginDto;
import com.southwestasiafloat.blog.dto.UserRegisterDto;
import com.southwestasiafloat.blog.service.AuthService;
import com.southwestasiafloat.blog.vo.AuthVo;
import com.southwestasiafloat.blog.vo.UserVo;
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
    public Result<AuthVo> login(@RequestBody UserLoginDto dto) throws Exception {
        return Result.ok(authService.login(dto));
    }

    @PostMapping({ "/refresh"})
    public Result<AuthVo> refresh(@RequestBody TokenRefreshDto dto) throws Exception {
        return Result.ok(authService.refresh(dto.getRefreshToken(), dto.getIp(), dto.getUserAgent()));
    }

    @PostMapping({ "/logout"})
    public Result<Void> logout(@RequestBody TokenLogoutDto dto) throws Exception {
        authService.logout(dto.getRefreshToken());
        return Result.ok(null);
    }
}

