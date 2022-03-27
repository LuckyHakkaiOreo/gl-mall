package com.winster.common.to.coupon;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 秒杀活动场次
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:04:23
 */
@Data
public class SeckillSessionTo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * @TableField(exist = false) 说明该字段在数据库中并不存在
     */
    private List<SeckillSkuRelationTo> skuRelationEntities;

}
