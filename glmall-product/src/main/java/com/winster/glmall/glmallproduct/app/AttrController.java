package com.winster.glmall.glmallproduct.app;

import com.winster.common.utils.PageUtils;
import com.winster.common.utils.R;
import com.winster.glmall.glmallproduct.service.AttrService;
import com.winster.glmall.glmallproduct.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * 商品属性
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:08:19
 */
@RestController
@RequestMapping("glmallproduct/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    /**
     * 查询指定分类的基础属性列表
     */
    @RequestMapping("{attrType}/list/{catId}")
    // @RequiresPermissions("glmallproduct:attr:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("attrType") String attrType, @PathVariable("catId") Long catId) {
        PageUtils page = attrService.queryPage(params, catId, attrType);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    // @RequiresPermissions("glmallproduct:attr:info")
    public R info(@PathVariable("attrId") Long attrId) {

        AttrVo attr = attrService.getInfo(attrId);

        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("glmallproduct:attr:save")
    public R save(@RequestBody AttrVo attr) {

        attrService.save(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("glmallproduct:attr:update")
    public R update(@RequestBody AttrVo attr) {
        attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("glmallproduct:attr:delete")
    public R delete(@RequestBody Long[] attrIds) {

        attrService.removeAttr(attrIds);

        return R.ok();
    }

}
