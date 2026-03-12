package com.southwestasiafloat.blog.controller;

import com.southwestasiafloat.blog.common.Result;
import com.southwestasiafloat.blog.dto.UserUpdateDto;
import com.southwestasiafloat.blog.service.UserService;
import com.southwestasiafloat.blog.vo.UserVo;
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

    //@获取所有用户信息


    // 更新用户信息（PATCH：只更新请求体中提供的字段）
    @PatchMapping("/users/{id}")
    public Result<UserVo> updateUser(@PathVariable("id") Long id, @RequestBody UserUpdateDto dto) {
        return Result.ok(userService.update(id, dto));
    }
}