package com.winster.glmall.cart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/cartList.html")
    public String index() {
        return "cartList";
    }
}
