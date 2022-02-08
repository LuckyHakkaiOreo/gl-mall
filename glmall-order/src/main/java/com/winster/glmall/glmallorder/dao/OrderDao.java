package com.winster.glmall.glmallorder.dao;

import com.winster.glmall.glmallorder.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 07:45:50
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
