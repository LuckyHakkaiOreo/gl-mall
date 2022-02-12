package com.winster.glmall.glmallware.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PurchaseDoneItemReqVo implements Serializable {
    private static final long serialVersionUID = -320853272918894667L;
    /**
     * 采购项id
     */
    private Long itemId;
    /**
     * 采购最后的状态：成功、失败、异常（部分成功）
     */
    private Integer status;

    /**
     * 采购失败/异常的原因
     */
    private String reason;
}
