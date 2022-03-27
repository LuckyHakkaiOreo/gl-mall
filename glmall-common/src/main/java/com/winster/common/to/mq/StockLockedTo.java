package com.winster.common.to.mq;

import lombok.Data;

import java.util.List;

@Data
public class StockLockedTo {
    /**
     * 库存工作单id
     */
    private Long id;

    /**
     * 工作单详情id
     */
    private List<StockLockedDetailTo> detailIds;

}
