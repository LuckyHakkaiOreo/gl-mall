package com.winster.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * spu信息
 * 
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:08:19
 */
@Data
public class SpuInfoTo implements Serializable {

	private static final long serialVersionUID = -6411463513713016791L;
	/**
	 * 商品id
	 */
	private Long id;
	/**
	 * 商品名称
	 */
	private String spuName;

	/**
	 * 商品描述
	 */
	private String spuDescription;
	/**
	 * 所属分类id
	 */
	private Long catalogId;
	/**
	 * 品牌id
	 */
	private Long brandId;

	/**
	 * spu商品名称
	 */
	private String brandName;

	/**
	 * 
	 */
	private BigDecimal weight;
	/**
	 * 上架状态[0 - 下架，1 - 上架]
	 */
	private Integer publishStatus;
	/**
	 * 
	 */
	private Date createTime;
	/**
	 * 
	 */
	private Date updateTime;

}
