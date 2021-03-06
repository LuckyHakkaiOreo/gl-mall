package com.winster.glmall.glmallproduct.web;

import com.winster.glmall.glmallproduct.entity.CategoryEntity;
import com.winster.glmall.glmallproduct.service.CategoryService;
import com.winster.glmall.glmallproduct.vo.Catelog2V0;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.datasource.password}")
    private String ppp;

    @GetMapping({"/","/index.html"})
    public String index(Model model){
        List<CategoryEntity> level1 = categoryService.findFirstLevelCategory(1l,"handsome");
        model.addAttribute("categorys", level1);
        System.out.println(ppp);
        return "index";
    }

    @GetMapping("index/json/catalog.json")
    @ResponseBody
    public Map<String, List<Catelog2V0>> getCatalogJson(){
        Map<String, List<Catelog2V0>> map = categoryService.getCatalogJson();
        return map;
    }

    @GetMapping("/hello")
    @ResponseBody
    public String hello(){


        return "hello";
    }


}
