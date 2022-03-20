package com.winster.glmall.cart.vo;

import lombok.Data;

@Data
public class UserInfoTo {
    private Long userId;
    private String userKey;
    private Boolean tempUser = false;
}
