package com.southwestasiafloat.blog.exception;

/** 参数或请求不合法 */
public class BadRequestException extends RuntimeException {
    public BadRequestException() { super(); }
    public BadRequestException(String message) { super(message); }
}

