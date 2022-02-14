package com.winster.common.to;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品库存
 * 
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 07:55:41
 */
@Data
public class WareSkuTo implements Serializable {

	private static final long serialVersionUID = -383969065833637115L;
	/**
	 * id
	 */
	private Long id;
	/**
	 * sku_id
	 */
	private Long skuId;
	/**
	 * 仓库id
	 */
	private Long wareId;
	/**
	 * 库存数
	 */
	private Integer stock;
	/**
	 * sku_name
	 */
	private String skuName;
	/**
	 * 锁定库存
	 */
	private Integer stockLocked;

}
