package com.winster.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SpuBoundsTo implements Serializable {

    private static final long serialVersionUID = 8207913080892657385L;

    private Long id;
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
    private Integer work;
}
