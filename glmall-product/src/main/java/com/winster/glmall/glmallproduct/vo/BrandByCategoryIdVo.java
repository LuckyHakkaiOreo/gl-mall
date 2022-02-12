package com.winster.glmall.glmallproduct.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BrandByCategoryIdVo implements Serializable {
    Long brandId;
    String brandName;
}
