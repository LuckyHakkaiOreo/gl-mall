package com.winster.glmall.glmallproduct.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catelog2V0 {
    private String catalogId;
    private List<Catelog3V0> catalog3List;
    private String id;
    private String name;

    /**
     * 三级分类vo
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catelog3V0 {
        private String catalogId;
        private String id;
        private String name;
    }
}
