package com.winster.glmall.glmallcoupon.controller;

import com.winster.common.utils.PageUtils;
import com.winster.common.utils.R;
import com.winster.glmall.glmallcoupon.entity.SeckillSessionEntity;
import com.winster.glmall.glmallcoupon.service.SeckillSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 秒杀活动场次
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:04:23
 */
@RestController
@RequestMapping("glmallcoupon/seckillsession")
public class SeckillSessionController {
    @Autowired
    private SeckillSessionService seckillSessionService;


    @GetMapping("/getLatest3DaySeckillSession")
    public R getLatest3DaySeckillSession(){
        List<SeckillSessionEntity> sessionEntities = seckillSessionService.getLatest3DaySeckillSession();
        return R.ok().put("data", sessionEntities);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("glmallcoupon:seckillsession:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = seckillSessionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("glmallcoupon:seckillsession:info")
    public R info(@PathVariable("id") Long id){
		SeckillSessionEntity seckillSession = seckillSessionService.getById(id);

        return R.ok().put("seckillSession", seckillSession);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("glmallcoupon:seckillsession:save")
    public R save(@RequestBody SeckillSessionEntity seckillSession){
		seckillSessionService.save(seckillSession);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("glmallcoupon:seckillsession:update")
    public R update(@RequestBody SeckillSessionEntity seckillSession){
		seckillSessionService.updateById(seckillSession);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("glmallcoupon:seckillsession:delete")
    public R delete(@RequestBody Long[] ids){
		seckillSessionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
