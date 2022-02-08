package com.winster.glmall.glmallproduct.controller;

import com.winster.common.utils.PageUtils;
import com.winster.common.utils.R;
import com.winster.common.valid.AddGroup;
import com.winster.common.valid.UpdateGroup;
import com.winster.glmall.glmallproduct.entity.BrandEntity;
import com.winster.glmall.glmallproduct.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 品牌
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 08:08:19
 */
@RestController
@RequestMapping("glmallproduct/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("glmallproduct:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    // @RequiresPermissions("glmallproduct:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("glmallproduct:brand:save")
    public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand){
		brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("glmallproduct:brand:update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand/*, BindingResult bindingResult*/){
        /*
        如果在这里这样写校验，那么会造成大量的重复代码，工作量会大大增加
        那么这里就不写错误的判断处理了，而是让其抛出异常，然后进行全局异常捕获
        if (bindingResult.hasErrors()) {
            List<FieldError> allErrors = bindingResult.getFieldErrors();
            HashMap<String, String> map = new HashMap<>();

            allErrors.forEach(objectError -> {
                String code = objectError.getCode();
                String field = objectError.getField();
                String defaultMessage = objectError.getDefaultMessage();
                map.put(field, defaultMessage);
            });
            return R.error().put("data", map);
        }*/

        brandService.updateCascade(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("glmallproduct:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
