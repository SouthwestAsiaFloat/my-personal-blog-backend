package com.southwestasiafloat.blog.controller;

import com.southwestasiafloat.blog.common.Result;
import com.southwestasiafloat.blog.dto.UserUpdateDto;
import com.southwestasiafloat.blog.service.UserService;
import com.southwestasiafloat.blog.utils.AuthContextUtil;
import com.southwestasiafloat.blog.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
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
    public Result<UserVo> updateUser(@PathVariable("id") Long id,
                                     @RequestBody UserUpdateDto dto,
                                     HttpServletRequest request) {
        if (!AuthContextUtil.isSelfOrAdmin(request, id)) {
            return Result.error(403, "无权限修改该用户信息");
        }

        // 只有管理员可以改角色，避免普通用户把自己改成 ADMIN。
        if (!AuthContextUtil.isAdmin(request) && dto != null && dto.getRole() != null) {
            return Result.error(403, "无权限修改用户角色");
        }

        return Result.ok(userService.update(id, dto));
    }
}