package com.winster.glmall.glmallorder.enums;

public enum OrderStatusEnum {

    CREATE_NEW(0, "待付款"),
    PAYED(0, "已付款"),
    SENDED(0, "已发货"),
    RECIEVED(0, "已完成"),
    CANCELED(0, "已取消"),
    SERVICING(0, "售后中"),
    SERVICED(0, "售后完成");

    private Integer code;
    private String msg;

    OrderStatusEnum(Integer code, String msg) {
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
