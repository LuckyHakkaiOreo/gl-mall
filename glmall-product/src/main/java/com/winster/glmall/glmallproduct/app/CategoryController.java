package com.winster.glmall.glmallproduct.app;

import com.winster.common.utils.PageUtils;
import com.winster.common.utils.R;
import com.winster.glmall.glmallproduct.entity.CategoryEntity;
import com.winster.glmall.glmallproduct.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品三级分类
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:08:19
 */
@RestController
@RequestMapping("glmallproduct/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @RequestMapping("feign/test")
    public R test(){
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName("测试分类");
        return R.ok().put("page", categoryEntity);
    }

    /**
     * 返回分类树，每一个分类都涵盖自己的下级分类
     * @return
     */
    @RequestMapping("/listWithTree")
    public R listWithTree(){
        List<CategoryEntity> listLevel1 = categoryService.listWithTree();
        return R.ok().put("list", listLevel1);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("glmallproduct:category:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    // @RequiresPermissions("glmallproduct:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("glmallproduct:category:save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("glmallproduct:category:update")
    public R update(@RequestBody CategoryEntity category){
        categoryService.updateCascade(category);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("glmallproduct:category:delete")
    public R delete(@RequestBody Long[] catIds){

        categoryService.removeCategories(Arrays.asList(catIds));
        return R.ok();
    }

}
