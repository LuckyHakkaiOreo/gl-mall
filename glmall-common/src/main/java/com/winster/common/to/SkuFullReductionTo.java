package com.winster.common.to;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品满减信息
 * 
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:04:23
 */
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSkuId() {
		return skuId;
	}

	public void setSkuId(Long skuId) {
		this.skuId = skuId;
	}

	public BigDecimal getFullPrice() {
		return fullPrice;
	}

	public void setFullPrice(BigDecimal fullPrice) {
		this.fullPrice = fullPrice;
	}

	public BigDecimal getReducePrice() {
		return reducePrice;
	}

	public void setReducePrice(BigDecimal reducePrice) {
		this.reducePrice = reducePrice;
	}

	public Integer getAddOther() {
		return addOther;
	}

	public void setAddOther(Integer addOther) {
		this.addOther = addOther;
	}
}
