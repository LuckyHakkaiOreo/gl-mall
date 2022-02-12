package com.winster.glmall.glmallproduct.vo;

import com.winster.glmall.glmallproduct.entity.AttrEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class AttrGroupWithAttrVo implements Serializable {
    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    /**
     * 当前分组下的所有属性
     */
    private List<AttrEntity> attrs = new ArrayList<>();
}
