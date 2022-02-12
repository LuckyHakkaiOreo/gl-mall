package com.winster.common.to;

import java.io.Serializable;
import java.math.BigDecimal;

public class SpuBoundsTo implements Serializable {

    private static final long serialVersionUID = 8207913080892657385L;

    private Long id;
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
    private Integer work;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSpuId() {
        return spuId;
    }

    public void setSpuId(Long spuId) {
        this.spuId = spuId;
    }

    public BigDecimal getBuyBounds() {
        return buyBounds;
    }

    public void setBuyBounds(BigDecimal buyBounds) {
        this.buyBounds = buyBounds;
    }

    public BigDecimal getGrowBounds() {
        return growBounds;
    }

    public void setGrowBounds(BigDecimal growBounds) {
        this.growBounds = growBounds;
    }

    public Integer getWork() {
        return work;
    }

    public void setWork(Integer work) {
        this.work = work;
    }
}
