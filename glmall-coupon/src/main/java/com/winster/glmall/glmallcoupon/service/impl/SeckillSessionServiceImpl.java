package com.winster.glmall.glmallcoupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.glmall.glmallcoupon.dao.SeckillSessionDao;
import com.winster.glmall.glmallcoupon.entity.SeckillSessionEntity;
import com.winster.glmall.glmallcoupon.entity.SeckillSkuRelationEntity;
import com.winster.glmall.glmallcoupon.service.SeckillSessionService;
import com.winster.glmall.glmallcoupon.service.SeckillSkuRelationService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Resource
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaySeckillSession() {
        // 获取最近三天的时间的秒杀活动
        List<SeckillSessionEntity> sessionEntityList = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", getStartTimeLatest3Day(), getEndTimeLatest3Day()));
        if (!CollectionUtils.isEmpty(sessionEntityList)) {
            // 查询所有关联的商品
            List<Long> ids = sessionEntityList.stream().map(SeckillSessionEntity::getId).collect(Collectors.toList());
            List<SeckillSkuRelationEntity> seckillSkuRelationEntityList = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().in("promotion_session_id", ids));
            Map<Long, List<SeckillSkuRelationEntity>> map = seckillSkuRelationEntityList.stream().collect(Collectors.groupingBy(SeckillSkuRelationEntity::getPromotionSessionId));
            // 为所有活动设置关联的商品
            sessionEntityList.forEach(seckillSessionEntity -> seckillSessionEntity.setSkuRelationEntities(map.get(seckillSessionEntity.getId())));
        }
        return sessionEntityList;
    }

    private String getStartTimeLatest3Day() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String getEndTimeLatest3Day() {
        return LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.MAX).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}