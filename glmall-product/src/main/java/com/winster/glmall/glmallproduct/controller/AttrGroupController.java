package com.winster.glmall.glmallproduct.controller;

import com.winster.common.utils.PageUtils;
import com.winster.common.utils.R;
import com.winster.glmall.glmallproduct.entity.AttrGroupEntity;
import com.winster.glmall.glmallproduct.service.AttrGroupService;
import com.winster.glmall.glmallproduct.service.CategoryService;
import com.winster.glmall.glmallproduct.vo.AttrGroupWithAttrVo;
import com.winster.glmall.glmallproduct.vo.AttrNoRelationVo;
import com.winster.glmall.glmallproduct.vo.AttrVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 属性分组
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:08:19
 */
@RestController
@RequestMapping("glmallproduct/attrgroup")
public class AttrGroupController {
    @Resource
    private AttrGroupService attrGroupService;

    @Resource
    private CategoryService categoryService;

    //     // /product/attrgroup/{catelogId}/withattr
    /**
     * 获取当前分类下，所有分组&属性数据
     * /attr/relation
     *
     * */
    @RequestMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrByCatId(@PathVariable("catelogId")  Long catelogId) {
        List<AttrGroupWithAttrVo> list = attrGroupService.getAttrGroupWithAttrByCatId(catelogId);

        return R.ok().put("data", list);
    }

    /**
     * 新增分组与属性关联
     * /attr/relation
    *
    * */
    @PostMapping("/attr/relation")
    public R saveAttrNoRelation(@RequestBody  List<Map<String, Object>> params) {

        R r = attrGroupService.saveAttrNoRelation(params);

        return r;
    }

    /**
     * 查询当前分类下未被关联的属性
     */
    @RequestMapping("{attrGroupId}/noattr/relation")
    public R findAttrNoRelation(AttrNoRelationVo vo, @PathVariable("attrGroupId") Long attrGroupId) {

        PageUtils page = attrGroupService.findAttrNoRelation(vo, attrGroupId);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    // @RequiresPermissions("glmallproduct:attrgroup:list")
    public R listByCate(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId) {
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("glmallproduct:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrGroupService.queryPage(params);

        return R.ok().put("page", page);
    }

    ///1/attr/relation
    @RequestMapping("/{attrGroupId}/attr/relation")
// @RequiresPermissions("glmallproduct:attrgroup:info")
    public R attrByAttrGroupId(@PathVariable("attrGroupId") Long attrGroupId) {
        List<AttrVo> list = attrGroupService.getAttrByAttrGroupId(attrGroupId);

        return R.ok().put("data", list);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    // @RequiresPermissions("glmallproduct:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();

        Long[] path = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("glmallproduct:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("glmallproduct:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除属性及其分组关系
     */
    @RequestMapping("/attr/relation/delete")
    // @RequiresPermissions("glmallproduct:attrgroup:delete")
    public R delete(@RequestBody List<Map<String, Object>> params) {

        attrGroupService.removeAttrRelation(params);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("glmallproduct:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
