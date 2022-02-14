package com.winster.glmall.glmallproduct.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.R;
import com.winster.glmall.glmallproduct.entity.CategoryBrandRelationEntity;
import com.winster.glmall.glmallproduct.service.CategoryBrandRelationService;
import com.winster.glmall.glmallproduct.vo.BrandByCategoryIdVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 品牌分类关联
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:08:19
 */
@RestController
@RequestMapping("glmallproduct/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    // /glmallproduct/categorybrandrelation/brands/list

    /**
     * 查询当前分类下，所有的品牌信息
     */
    @RequestMapping("/brands/list")
    // @RequiresPermissions("glmallproduct:categorybrandrelation:list")
    public R getBrandsListByCatId(@RequestParam Long catId) {
        /*
        {
	"msg": "success",
	"code": 0,
	"data": [{
		"brandId": 0,
		"brandName": "string",
	}]
}
        * */

        List<CategoryBrandRelationEntity> list = categoryBrandRelationService.getBrandsListByCatId(catId);

        List<BrandByCategoryIdVo> result = list.stream().map(b -> {
            BrandByCategoryIdVo vo = new BrandByCategoryIdVo();
            vo.setBrandId(b.getBrandId());
            vo.setBrandName(b.getBrandName());
            return vo;
        }).collect(Collectors.toList());

        return R.ok().put("data", result);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("glmallproduct:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }

    @RequestMapping("/catelogList")
    // @RequiresPermissions("glmallproduct:categorybrandrelation:list")
    public R catelogList(Long brandId) {

        QueryWrapper<CategoryBrandRelationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("brand_id", brandId);
        List<CategoryBrandRelationEntity> list = categoryBrandRelationService.list(wrapper);

        return R.ok().put("data", list);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("glmallproduct:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("glmallproduct:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {

        categoryBrandRelationService.saveCategoryBrandRelation(categoryBrandRelation);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("glmallproduct:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("glmallproduct:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids) {
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
