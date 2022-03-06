package com.winster.thirdpartyserver.es.controller;

import com.winster.common.exception.BizCodeEnum;
import com.winster.common.to.es.SkuESTo;
import com.winster.common.utils.R;
import com.winster.thirdpartyserver.es.service.ESSkuInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/es/opt")
public class ESSkuInfoController {

    @Resource
    private ESSkuInfoService esSkuInfoService;

    @PostMapping("/save/skuinfos")
    public R saveSkuESTos(@RequestBody List<SkuESTo> list) throws IOException {
        Boolean b = esSkuInfoService.saveSkuESTos(list);

        if (b) {
            return R.ok();
        }
        return R.error(BizCodeEnum.THIRD_ES_SAVE_EXCEPTION);
    }
}
