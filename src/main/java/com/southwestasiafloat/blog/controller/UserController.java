package com.southwestasiafloat.blog.controller;

import com.southwestasiafloat.blog.entity.User;
import com.southwestasiafloat.blog.common.Result;
import com.southwestasiafloat.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/users/{id}")
    public Result<User> getUserById(@PathVariable("id") Long id) {
        return userService.getById(id)
                .map(Result::ok)
                .orElse(Result.error(404, "not found"));
    }
}
