package com.winster.glmall.glmallproduct.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.winster.glmall.glmallproduct.entity.CategoryEntity;
import com.winster.glmall.glmallproduct.service.CategoryService;
import com.winster.glmall.glmallproduct.vo.Catelog2V0;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Resource
    private CategoryService categoryService;

    @GetMapping({"/","/index.html"})
    public String index(Model model){
        List<CategoryEntity> level1 = categoryService.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        model.addAttribute("categorys", level1);
        return "index";
    }

    @GetMapping("index/catalog.json")
    @ResponseBody
    public Map<String, List<Catelog2V0>> getCatalogJson(){
        Map<String, List<Catelog2V0>> map = categoryService.getCatalogJson();
        return map;
    }


}
