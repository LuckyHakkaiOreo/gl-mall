package com.winster.common.constant;

public class WareConstant {

    public enum PurchaseStatusEnum {
        // '属性类型[0-销售属性，1-基本属性，2-既是销售属性又是基本属性]'
        STATUS_CREATED(0, "created", "新建"),
        STATUS_ASSIGNED(1, "assigned", "已分配"),
        STATUS_RECEIVED(2, "received", "已领取"),
        STATUS_FINISHED(3, "finished", "已完成"),
        STATUS_EXCEPTION(4, "exception", "有异常");

        private Integer code;
        private String flag;
        private String desc;

        PurchaseStatusEnum(Integer code, String flag, String desc) {
            this.code = code;
            this.flag = flag;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getFlag() {
            return flag;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum PurchaseDetailStatusEnum {
        // 状态[0新建，1已分配，2正在采购，3已完成，4采购失败]
        STATUS_CREATED(0, "created", "新建"),
        STATUS_ASSIGNED(1, "assigned", "已分配"),
        STATUS_BUYING(2, "buying", "正在采购"),
        STATUS_FINISHED(3, "finished", "已完成"),
        STATUS_EXCEPTION(4, "failure", "采购失败");

        private Integer code;
        private String flag;
        private String desc;

        PurchaseDetailStatusEnum(Integer code, String flag, String desc) {
            this.code = code;
            this.flag = flag;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getFlag() {
            return flag;
        }

        public String getDesc() {
            return desc;
        }
    }
}
