package com.winster.glmall.glmallmember.exception;

public class PhoneExistException extends RuntimeException{
    public PhoneExistException() {
        super("手机号已存在");
    }

    public PhoneExistException(String message) {
        super(message);
    }
}
