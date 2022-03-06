package com.winster.common.to;

import lombok.Data;

@Data
public class UserRegisterTo {
    private String userName;

    private String password;

    private String phone;

    private String code;
}
