package com.winster.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品阶梯价格
 * 
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:04:23
 */
@Data
public class SkuLadderTo implements Serializable {

	private static final long serialVersionUID = 2717495919296261422L;
	/**
	 * id
	 */
	private Long id;
	/**
	 * spu_id
	 */
	private Long skuId;
	/**
	 * 满几件
	 */
	private Integer fullCount;
	/**
	 * 打几折
	 */
	private BigDecimal discount;
	/**
	 * 折后价
	 */
	private BigDecimal price;
	/**
	 * 是否叠加其他优惠[0-不可叠加，1-可叠加]
	 */
	private Integer addOther;

}
