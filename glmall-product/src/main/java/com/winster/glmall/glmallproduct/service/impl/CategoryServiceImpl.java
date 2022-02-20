package com.winster.glmall.glmallproduct.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.glmall.glmallproduct.dao.CategoryBrandRelationDao;
import com.winster.glmall.glmallproduct.dao.CategoryDao;
import com.winster.glmall.glmallproduct.entity.CategoryBrandRelationEntity;
import com.winster.glmall.glmallproduct.entity.CategoryEntity;
import com.winster.glmall.glmallproduct.service.CategoryService;
import com.winster.glmall.glmallproduct.vo.Catelog2V0;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 算法分析，其实这里分类的算法思路有两种：
     * 一、写递归函数，从上到下遍历寻找子分类；（这种写法比较复杂）
     * 二、使用java8 stream函数对【所有分类的列表】按照【parentId】进行分类，
     * 然后再为每一个分类从hashMap中找出并设置其子分类；（这种写法思路比较简单）
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        // 1.查询所有分类
        List<CategoryEntity> all = baseMapper.selectList(null);
        // 2.将所有分类按照parentId分组
        Map<Long, List<CategoryEntity>> map = all.stream().collect(Collectors.groupingBy(CategoryEntity::getParentCid));

        // 3.为每一个分类设置其子分类列表并且排序
        all = all.stream()
                // 没有下级分类的不需要设置子分类，直接过滤掉
                .filter(categoryEntity -> map.get(categoryEntity.getCatId()) != null)
                // 在分类map中为所有分类设置子分类（子分类也需要排序）
                .map(categoryEntity -> {
                    categoryEntity.setChildren(map.get(categoryEntity.getCatId()).stream()
                            .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                            .collect(Collectors.toList()));
                    return categoryEntity;
                })
                // 仅返回一级分类的数据
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                // 对所有一级分类进行排序
                .sorted(Comparator.comparingInt(categoryEntity -> (categoryEntity.getSort() == null ? 0 : categoryEntity.getSort())))
                .collect(Collectors.toList());

        return all;
    }

    @Override
    public void removeCategories(List<Long> asList) {
        // TODO 1. 检查当前删除的菜单是否有被引用

        // 逻辑删除
        /*
        1.配置全局的逻辑删除（可省略）
        2.低版本需要配置逻辑删除的组件（可省略）
        3.在需要作为逻辑删除的实体类字段上加注解：@TableLogic(value = "1", delval = "0")
        value表示不删除的值，delval表示删除的值
        * */
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> list = new ArrayList<>();
        findCatePath(catelogId, list);

        Collections.reverse(list);

        return list.toArray(new Long[list.size()]);
    }

    @CacheEvict(value = "category",key = "'findFirstLevelCategory'")
//    @CacheEvict(value = "category",allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        baseMapper.updateById(category);

        // 如果分类名字不为空，则级联修改品牌分类关系表的分类名称
        if (StringUtils.isNotBlank(category.getName())) {
            // 级联删除其他关联了brand表中brandName字段的表
            QueryWrapper<CategoryBrandRelationEntity> cbWrapper = new QueryWrapper<>();
            cbWrapper.eq("catelog_id", category.getCatId());
            CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
            entity.setCatelogName(category.getName());
            categoryBrandRelationDao.update(entity, cbWrapper);
        }
    }

    /**
     * 1.如果想模拟缓存击穿，可以使用jmeter并发请求这个接口，然后突然某一时刻，将redis的key删除，
     * 此时大量的并发访问会达到数据库，
     *
     * @return
     */
    @Override
    public Map<String, List<Catelog2V0>> getCatalogJson() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");

        if (StringUtils.isBlank(catalogJson)) {
            log.info("没有缓存或者第一次请求分类数据....");
//            Map<String, List<Catelog2V0>> catalogJsonFromDB = getCatalogJsonFromDBWithLocalLock();
//            Map<String, List<Catelog2V0>> catalogJsonFromDB = getCatalogJsonFromDBWithRedisLock();
            Map<String, List<Catelog2V0>> catalogJsonFromDB = getCatalogJsonFromDBWithRedisson();
            return catalogJsonFromDB;
        }
        Map<String, List<Catelog2V0>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2V0>>>() {
        });
        return result;
    }

    /**
     * 一、引包：
     * <dependency>
     *        <groupId>org.springframework.boot</groupId>
     *        <artifactId>spring-boot-starter-cache</artifactId>
     * </dependency>
     * 二、启动类加注解
     * @EnableCaching
     *
     * @Cacheable 的默认行为：
     * 1、@Cacheable({"缓存名字"})，
     * 如果没有缓存数据，先调用方法后，将结果缓存；
     * 如果存在缓存数据，直接返回缓存结果，不调用方法。
     * 2、在缓存中间件（redis）中，自动生成的key 与 cacheName和实际参数有关：
     * category::SimpleKey [1,handsome]
     * 3、缓存值默认使用java序列化机制，缓存序列化后的数据
     * 4、默认时间ttl：-1，相当于永不过期
     * 可自定义的行为：
     * 1、key，接受SpEL表达式，参考注解源码注释：
     * {@code #root.method}, {@code #root.target}, and {@code #root.caches}
     * 2、指定key的存活时间：spring.redis.time-to-live，单位是毫秒
     * 3、将结果保存为json格式字符串：
     * @param id
     * @param name
     * @return
     */
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> findFirstLevelCategory(Long id, String name) {
        return this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    public Map<String, List<Catelog2V0>> getCatalogJsonFromDBWithRedisson() {
        // 使用redisson完成分布式锁
        RLock lock = redissonClient.getLock("catalogJson-lock");

        // 多个线程会在这里被锁住
        lock.lock();
        Map<String, List<Catelog2V0>> result = null;
        try {
            result = getcatalogJsonFromDB();
        } finally {
            lock.unlock();
        }

        return result;
    }

    public Map<String, List<Catelog2V0>> getCatalogJsonFromDBWithRedisLock() {
        // 获取分布式锁的代码
        //---> redis-cli: set key val EX 300 NX
        /** 这里为什么使用uuid？
         * 假设：第一个线程业务处理时间过长，锁自动失效；
         第二个自旋线程加锁成功，进来执行业务；
         然后，第一个线程先结束，释放锁；第二个自旋线程加的锁也会被释放，
         导致会有第三个新的线程加锁成功进来执行业务；
         以此类推...最终会造成分布式锁的连锁崩溃，每一个自旋线程都排队查询了一遍数据库...
         综上，我们需要针对每一个执行线程，使用不同的值来作为锁的value
         */
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        Map<String, List<Catelog2V0>> result = null;
        if (lock) {
            log.info("获取分布式锁成功，去数据库查询分类数据！");
            try {
                result = getcatalogJsonFromDB();
            } finally {
                //删除锁，为了保证删除锁是原子操作，我们使用lua脚本来完成锁删除
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then  return redis.call('del', KEYS[1]) else return 0 end";
                Long rs = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class)
                        , Arrays.asList("lock"), uuid);
            }
        } else {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
            getCatalogJsonFromDBWithRedisLock();
        }

        return result;
    }

    public Map<String, List<Catelog2V0>> getCatalogJsonFromDBWithLocalLock() {

        // 一次全量查出所有的分类数据，这样能够较大的提高接口的吞吐量。

        /**
         * 为了防止缓存击穿，我们可以在缓存失效后，需要从数据库中查询数据之前，加上锁
         * 假设，catalogJson缓存失效的瞬间，有100个w的请求来到了这里想查数据库数据。
         * 如果都直接放行，那么将会导致数据库压力骤升，甚至崩溃；那么在查询数据库之前，
         * 我们加上本地锁synchronized只放行一个请求进去：
         * 先判断缓存没有数据，才查数据库；
         * 缓存有数据，直接读缓存返回。
         *
         * 总结：本地锁防止缓存击穿的方法不是特别好，但是也不是特别坏，主要得看具体的业务需求
         * 像我们这里的查询分类需求使用本地锁是一种成立的解决方案
         */
        synchronized (this) {
            Map<String, List<Catelog2V0>> result = getcatalogJsonFromDB();
            return result;
        }
    }

    private Map<String, List<Catelog2V0>> getcatalogJsonFromDB() {

        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isNotBlank(catalogJson)) {
            log.info("缓存失效后，已经被其他线程重新缓存....");
            Map<String, List<Catelog2V0>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2V0>>>() {
            });
            return result;
        }

        log.info("查询数据库，获取分类数据....");
        List<CategoryEntity> all = this.list();
        // 1级分类
        List<CategoryEntity> level1 = findCategorysByParentId(all, 0l);
        Map<String, List<Catelog2V0>> result = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), i1 -> {
            //1. 查出当前所有二级分类
            List<CategoryEntity> level2 = findCategorysByParentId(all, i1.getCatId());
            List<Catelog2V0> catelog2V0s = null;
            if (!CollectionUtils.isEmpty(level2)) {
                catelog2V0s = level2.stream().map(i2 -> {
                    Catelog2V0 catelog2V0 = new Catelog2V0(i1.getCatId().toString(), null, i2.getCatId().toString(), i2.getName());
                    // 查询当前二级分类的三级分类
                    List<CategoryEntity> level3 = findCategorysByParentId(all, i2.getCatId());
                    if (!CollectionUtils.isEmpty(level3)) {
                        List<Catelog2V0.Catelog3V0> catelog3V0s = level3.stream().map(i3 -> {
                            Catelog2V0.Catelog3V0 catelog3V0 = new Catelog2V0.Catelog3V0(i2.getCatId().toString(), i3.getCatId().toString(), i3.getName());
                            return catelog3V0;
                        }).collect(Collectors.toList());
                        catelog2V0.setCatalog3List(catelog3V0s);
                    }

                    return catelog2V0;
                }).collect(Collectors.toList());
            }
            return catelog2V0s;
        }));
        // 将结果存入缓存中
        stringRedisTemplate.opsForValue().set("catalogJson", JSON.toJSONString(result), 1, TimeUnit.DAYS);
        return result;
    }

    private List<CategoryEntity> findCategorysByParentId(List<CategoryEntity> all, Long parentId) {
        return all.stream().filter(item -> item.getParentCid() == parentId).collect(Collectors.toList());
    }

    private void findCatePath(Long catelogId, List<Long> list) {
        list.add(catelogId);
        CategoryEntity entity = baseMapper.selectById(catelogId);
        if (entity.getParentCid() != null && entity.getParentCid() != 0) {
            findCatePath(entity.getParentCid(), list);
        }
    }

}