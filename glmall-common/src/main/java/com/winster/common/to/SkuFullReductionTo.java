package com.winster.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品满减信息
 * 
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:04:23
 */
@Data
public class SkuFullReductionTo implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Long id;
	/**
	 * spu_id
	 */
	private Long skuId;
	/**
	 * 满多少
	 */
	private BigDecimal fullPrice;
	/**
	 * 减多少
	 */
	private BigDecimal reducePrice;
	/**
	 * 是否参与其他优惠
	 */
	private Integer addOther;


}
