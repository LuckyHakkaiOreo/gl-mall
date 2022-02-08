package com.winster.glmall.glmallproduct;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.winster.glmall.glmallproduct.entity.CategoryEntity;
import com.winster.glmall.glmallproduct.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;

@Slf4j
@SpringBootTest
class GlmallProductApplicationTests {

    @Resource
    private CategoryService categoryService;

    @Test
    void testFindPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info(Arrays.toString(catelogPath));
    }

    @Test
    void contextLoads() {
        CategoryEntity entity = new CategoryEntity();
        entity.setName("生活用品");
        boolean save = categoryService.save(entity);

        Assert.assertEquals(save, true);

        QueryWrapper<CategoryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", "生活用品");
        CategoryEntity one = categoryService.getOne(queryWrapper);
        Assert.assertNotNull(one);

        one.setSort(6);
        boolean b = categoryService.updateById(one);
        Assert.assertEquals(b, true);
    }

}
