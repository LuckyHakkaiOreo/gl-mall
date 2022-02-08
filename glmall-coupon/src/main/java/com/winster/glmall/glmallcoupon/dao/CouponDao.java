package com.winster.glmall.glmallcoupon.dao;

import com.winster.glmall.glmallcoupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:04:23
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
