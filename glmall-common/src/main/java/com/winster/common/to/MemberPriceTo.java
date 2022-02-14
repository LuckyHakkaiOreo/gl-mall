package com.winster.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品会员价格
 * 
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:04:23
 */
@Data
public class MemberPriceTo implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Long id;
	/**
	 * sku_id
	 */
	private Long skuId;
	/**
	 * 会员等级id
	 */
	private Long memberLevelId;
	/**
	 * 会员等级名
	 */
	private String memberLevelName;
	/**
	 * 会员对应价格
	 */
	private BigDecimal memberPrice;
	/**
	 * 可否叠加其他优惠[0-不可叠加优惠，1-可叠加]
	 */
	private Integer addOther;

}
