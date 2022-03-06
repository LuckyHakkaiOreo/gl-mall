package com.winster.glmall.glmallproduct.web;

import com.winster.glmall.glmallproduct.service.SkuInfoService;
import com.winster.glmall.glmallproduct.vo.SkuItemVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@Slf4j
@Controller
public class ItemController {

    @Resource
    private SkuInfoService skuInfoService;

    /**
     * 展示当前sku的详情
     *
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        log.info("展示sku{}的详情...", skuId);

        SkuItemVo vo = skuInfoService.item(skuId);
        model.addAttribute("item", vo);

        return "item";
    }
}
