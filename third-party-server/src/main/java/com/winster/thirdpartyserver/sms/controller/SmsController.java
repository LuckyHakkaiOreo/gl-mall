package com.winster.thirdpartyserver.sms.controller;

import com.winster.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/sms")
public class SmsController {

    /**
     * 提供给外部所有服务进行调用的
     *
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        // TODO 没有短信平台，暂时跳过
        log.info("发送短信验证码：{}到手机：{}", code, phone);
        return R.ok();
    }
}
