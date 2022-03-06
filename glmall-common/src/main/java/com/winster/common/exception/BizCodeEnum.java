package com.winster.common.exception;

/**
 * 错误码和错误信息定义类，针对系统中所有服务生效
 * 1.错误码长度为5位
 * 2.前两位表示的是业务场景，后三位表示错误码：例如，10001，10表示通用异常，000表示系统未知异常
 * 3.业务场景code详情：
 * 1）10，通用：
 * 000，系统未知异常
 * 001，参数校验未通过
 * 2）11，商品
 * 3）12，订单
 * 4）13，购物车
 * 5）14，物流
 * 6)  15.  三方服务
 * 7)  16.  认证服务
 * 8)  17.  会员服务
 */
public enum BizCodeEnum {
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_PARAMS_EXCEPTION(10001, "参数校验不通过"),
    THIRD_ES_SAVE_EXCEPTION(15000, "三方服务es保存发生错误"),
    AUTH_REPEAT_SEND_CODE_EXCEPTION(16000, "验证码获取频率太高"),
    MEMBER_PHONE_UNIQUE_EXCEPTION(17000, "会员服务，手机必须唯一"),
    MEMBER_USERNAME_UNIQUE_EXCEPTION(17001, "会员服务，用户名必须唯一"),
    MEMBER_LOGIN_USERNAME_PASSWPRD_EXCEPTION(17002, "账号密码错误");

    private Integer code;
    private String msg;

    BizCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
