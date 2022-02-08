package com.winster.glmall.glmallmember.dao;

import com.winster.glmall.glmallmember.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 07:34:31
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
