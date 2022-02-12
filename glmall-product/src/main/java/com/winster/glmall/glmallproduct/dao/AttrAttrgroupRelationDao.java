package com.winster.glmall.glmallproduct.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.winster.glmall.glmallproduct.entity.AttrAttrgroupRelationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 * 
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:08:19
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {
    void deleteBatchRelation(@Param("params") List<Map<String, Object>> params);

    void insertBatchRelation(@Param("params") List<Map<String, Object>> params);

//    void deleteBatchByAttrIdAndAttrGroupId(List<Map<String, Object>> params);
}
