package com.winster.glmall.glmallware.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PurchaseReceiveReqVo implements Serializable {

    private static final long serialVersionUID = -5668006282334913908L;
    /**
     * 领取采购单的人的id
     */
    private Long receiveId;
    /**
     * 采购单id集合
     */
    private List<Long> items;
}
