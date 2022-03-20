package com.winster.common.to;

import lombok.Data;

@Data
public class WareLockStockResult {
    private Long skuId;
    private Integer num;
    private Boolean locked;
}
