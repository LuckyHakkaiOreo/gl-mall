package com.winster.glmall.glmallware.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PurchaseMergeReqVo implements Serializable {
    private static final long serialVersionUID = 9002094640936047287L;

    private Long purchaseId;
    private List<Long> items;
}
