package com.southwestasiafloat.blog.exception;

/**
 * 认证 / 授权相关异常（例如登录失败、凭证错误）
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException() {
        super();
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

