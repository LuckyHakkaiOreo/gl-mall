package com.winster.glmall.glmallware.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PurchaseDoneReqVo implements Serializable {

    private static final long serialVersionUID = 5648837305917558792L;
    /**
     * 领取采购单的人的id
     */
    private Long id;
    /**
     * 采购单id集合
     */
    private List<PurchaseDoneItemReqVo> items;
}
