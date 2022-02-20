package com.winster.glmall.glmallproduct;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.winster.glmall.glmallproduct.entity.CategoryEntity;
import com.winster.glmall.glmallproduct.service.AttrGroupService;
import com.winster.glmall.glmallproduct.service.CategoryService;
import com.winster.glmall.glmallproduct.service.SkuSaleAttrValueService;
import com.winster.glmall.glmallproduct.vo.SkuItemVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@SpringBootTest
class GlmallProductApplicationTests {

    @Resource
    private CategoryService categoryService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private AttrGroupService attrGroupService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Test
    void testSaleAttr() {
        List<SkuItemVo.ItemSaleAttrVo> result = skuSaleAttrValueService.getSaleAttrsBySpuId(7l);
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    void testGroupAttr() {
        List<SkuItemVo.SpuItemAttrGroupVo> vos = attrGroupService.getAttrByAttrGroupWithAttrsBySpuId(225l, 18l);

        System.out.println(JSON.toJSONString(vos));
    }

    @Test
    void testRedisson() {
        redissonClient.getKeys().getKeys().forEach(System.out::println);

    }
    @Test
    void testRedis() {
        // k-v键值对
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        stringStringValueOperations.set("strKey", "strVal");
        String sV = stringStringValueOperations.get("strKey");
        // list 集合
        ListOperations<String, String> stringStringListOperations = stringRedisTemplate.opsForList();
        stringStringListOperations.leftPush("listK", "lv1");
        stringStringListOperations.leftPush("listK", "lv2");
        String listK = stringStringListOperations.rightPop("listK");
        // set无序集合
        SetOperations<String, String> stringStringSetOperations = stringRedisTemplate.opsForSet();
        stringStringSetOperations.add("sK", "sV1", "sV2", "sV3");
        String sv = stringStringSetOperations.randomMember("sK");
        // zSet有序集合
        ZSetOperations<String, String> stringStringZSetOperations = stringRedisTemplate.opsForZSet();
        stringStringZSetOperations.add("zsK", "zsV1", 1);
        stringStringZSetOperations.add("zsK", "zsV2", 1);
        stringStringZSetOperations.add("zsK", "zsV3", 1);
        Set<String> zsK = stringStringZSetOperations.range("zsK", 0, 3);
        System.out.println(Arrays.toString(zsK.toArray()));
        // hash表
        HashOperations<String, String, String> stringObjectObjectHashOperations = stringRedisTemplate.opsForHash();
        stringObjectObjectHashOperations.put("ht", "k1", "v1");
        stringObjectObjectHashOperations.put("ht", "k2", "v2");
        stringObjectObjectHashOperations.put("ht", "k3", "v3");
        Set<String> keys = stringObjectObjectHashOperations.keys("ht");
        System.out.println(Arrays.toString(keys.toArray()));
        List<String> values = stringObjectObjectHashOperations.values("ht");
        System.out.println(Arrays.toString(values.toArray()));


    }

    @Test
    void testFindPath() {
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
