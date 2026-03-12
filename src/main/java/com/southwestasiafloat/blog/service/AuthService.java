package com.southwestasiafloat.blog.service;

import com.southwestasiafloat.blog.dto.UserLoginDto;
import com.southwestasiafloat.blog.dto.UserRegisterDto;
import com.southwestasiafloat.blog.vo.AuthVo;
import com.southwestasiafloat.blog.vo.UserVo;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {

    AuthVo login(UserLoginDto dto) throws Exception;

    UserVo register(UserRegisterDto dto);

    @Transactional
    AuthVo refresh(String refreshToken, String ip, String userAgent) throws Exception;

    void logout(String refreshToken) throws Exception;
}

