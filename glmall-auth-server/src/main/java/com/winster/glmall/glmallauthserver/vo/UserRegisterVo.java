package com.winster.glmall.glmallauthserver.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class UserRegisterVo {
    @NotNull(message = "用户名必须提交")
    @Pattern(regexp = "^[a-zA-Z0-9-_]{6,18}$", message = "用户名必须是6-18位的字符")
    private String userName;

    @NotNull(message = "密码必须提交")
//    @Length(min = 6, max = 20, message = "必须是6-20位的字符")
    @Pattern(regexp = "^(?=.*[0-9].*)(?=.*[A-Z].*)(?=.*[a-z].*).{6,20}$",message = "字符串必须包含大写字母，小写字母和数字并长度在6-20")
    private String password;

    @NotNull(message = "手机号必须提交")
    @Pattern(regexp = "^[1][3-9][0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码必须提交")
    private String code;
}
