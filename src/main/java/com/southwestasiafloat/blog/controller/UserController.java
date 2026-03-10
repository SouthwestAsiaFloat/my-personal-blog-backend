package com.southwestasiafloat.blog.controller;

import com.southwestasiafloat.blog.common.Result;
import com.southwestasiafloat.blog.dto.TokenLogoutDto;
import com.southwestasiafloat.blog.dto.TokenRefreshDto;
import com.southwestasiafloat.blog.dto.UserLoginDto;
import com.southwestasiafloat.blog.dto.UserRegisterDto;
import com.southwestasiafloat.blog.dto.UserUpdateDto;
import com.southwestasiafloat.blog.service.UserService;
import com.southwestasiafloat.blog.vo.UserVo;
import com.southwestasiafloat.blog.vo.AuthVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    // 获取用户信息
    @GetMapping("/users/{id}")
    public Result<UserVo> getUserById(@PathVariable("id") Long id) {
        return userService.getById(id)
                .map(Result::ok)
                .orElse(Result.error(404, "not found"));
    }

    // 登录（使用对象请求体）
    @PostMapping("/login")
    public Result<AuthVo> login(@RequestBody UserLoginDto dto) throws Exception {
        AuthVo auth = userService.login(dto);
        return Result.ok(auth);
    }

    // 刷新 access token（并轮换 refresh token）
    @PostMapping("/refresh")
    public Result<AuthVo> refresh(@RequestBody TokenRefreshDto dto) throws Exception {
        AuthVo auth = userService.refresh(dto.getRefreshToken(), dto.getIp(), dto.getUserAgent());
        return Result.ok(auth);
    }

    // 注销：撤销 refresh token（幂等）
    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody TokenLogoutDto dto) throws Exception {
        userService.logout(dto.getRefreshToken());
        return Result.ok();
    }

    // 注册（使用 DTO 输入，VO 输出）
    @PostMapping("/register")
    public Result<UserVo> register(@RequestBody UserRegisterDto dto) {
        return Result.ok(userService.register(dto));
    }

    // 更新用户信息（PATCH：只更新请求体中提供的字段）
    @PatchMapping("/users/{id}")
    public Result<UserVo> updateUser(@PathVariable("id") Long id, @RequestBody UserUpdateDto dto) {
        return Result.ok(userService.update(id, dto));
    }
}