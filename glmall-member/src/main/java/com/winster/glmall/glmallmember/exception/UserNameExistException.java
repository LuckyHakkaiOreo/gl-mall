package com.winster.glmall.glmallmember.exception;

public class UserNameExistException extends RuntimeException{

    public UserNameExistException() {
        super("用户名已存在");
    }

    public UserNameExistException(String message) {
        super(message);
    }
}
