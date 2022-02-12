package com.winster.glmall.glmallproduct.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AttrNoRelationVo implements Serializable {
    private Integer page;
    private Integer limit;
    private String key;
}
