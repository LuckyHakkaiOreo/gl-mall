package com.winster.common.to;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * spu信息
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:08:19
 */
@Data
public class SpuInfoWithSkuIdTo implements Serializable {

    private static final long serialVersionUID = 4376284399357702982L;

    private SpuInfoTo spuInfo;

    private List<Long> skuIds;

}
