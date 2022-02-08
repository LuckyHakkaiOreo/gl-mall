package com.winster.glmall.glmallware;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.winster.glmall.glmallware.entity.WareInfoEntity;
import com.winster.glmall.glmallware.service.WareInfoService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class GlmallWareApplicationTests {

    @Resource
    private WareInfoService wareInfoService;

    @Test
    void contextLoads() {
        WareInfoEntity entity = new WareInfoEntity();
        entity.setName("北京货");
        boolean save = wareInfoService.save(entity);

        Assert.assertEquals(save,true);

        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name","北京货");
        WareInfoEntity one = wareInfoService.getOne(queryWrapper);
        Assert.assertNotNull(one);

        one.setAddress("北京市海淀区王府井");
        boolean b = wareInfoService.updateById(one);
        Assert.assertEquals(b, true);
    }

}
