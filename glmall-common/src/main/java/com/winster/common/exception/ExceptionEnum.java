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
 */
public enum ExceptionEnum {
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_PARAMS_EXCEPTION(10001, "参数校验不通过");

    private Integer code;
    private String msg;

    ExceptionEnum(Integer code, String msg) {
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
