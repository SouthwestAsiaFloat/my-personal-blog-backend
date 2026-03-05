package com.southwestasiafloat.blog.controller;

import com.southwestasiafloat.blog.entity.User;
import com.southwestasiafloat.blog.common.Result;
import com.southwestasiafloat.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
public class UserController {
    @Autowired
    private UserService userService;

    // 获取用户信息
    @GetMapping("/users/{id}")
    public Result<User> getUserById(@PathVariable("id") Long id) {
        return userService.getById(id)
                .map(Result::ok)
                .orElse(Result.error(404, "not found"));
    }

    // 登录
    @PostMapping("/login")
    public Result<Void> login(@RequestParam String username, @RequestParam String password) {
        if (username == null || password == null) {
            return Result.error(400, "username and password are required");
        }
        try {
            userService.login(username, password);
            return Result.ok();
        } catch (Exception e) {
            return Result.error(401, e.getMessage());
        }
    }

    // 注册
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
            return Result.error(400, "username, password and email are required");
        }
        try {
            User created = userService.register(user);
            return Result.ok(created);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    // 更新用户信息（通过请求体传递完整用户对象）
    @PatchMapping("/users/{id}")
    public Result<User> updateUser(@PathVariable("id") Long id, @RequestBody User user) {
        if (id == null || user == null) {
            return Result.error(400, "id 和用户数据不能为空");
        }
        try {
            User updated = userService.update(id, user);
            return Result.ok(updated);
        } catch (Exception e) {
            // 服务层已经抛出详细异常信息，这里直接返回
            return Result.error(400, e.getMessage());
        }
    }
}