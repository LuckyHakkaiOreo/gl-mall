package com.winster.common.constant;

public class ProductConstant {

    public enum ProductAttrEnum {
        // '属性类型[0-销售属性，1-基本属性，2-既是销售属性又是基本属性]'
        TYPE_ATTR_BASE(1, "base", "基本属性"),
        TYPE_ATTR_SALE(0, "sale", "销售属性");

        private Integer code;
        private String flag;
        private String desc;

        ProductAttrEnum(Integer code, String flag, String desc) {
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
