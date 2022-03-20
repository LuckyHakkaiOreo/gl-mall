package com.winster.glmall.cart.controller;

import com.winster.glmall.cart.service.CartService;
import com.winster.glmall.cart.vo.Cart;
import com.winster.glmall.cart.vo.CartItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.List;

@Controller
public class CartController {

    @Resource
    private CartService cartService;

    /**
     * 浏览器有一个user-key的cookie：用来标识用户临时信息，一个月后过期
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份
     * 浏览器保存以后，每次访问都会带上这个cookie
     * <p>
     * 登录：session有用户信息
     * 没登录：按照cookie中的user-key来标识临时用户
     * 第一次，没有临时用户，帮忙创建一个临时用户。
     *
     * @return
     */
    @RequestMapping("/cartList.html")
    public String index(Model model) {
        // spring 中，一次请求，我们从拦截器->controller->service->dao都是在同一个线程里操作
        // 获取用户的购物车列表
        Cart cart = cartService.getCartList();
        model.addAttribute("cart", cart);

        return "cartList";
    }

    /**
     * 添加商品到购物车
     *
     * @param skuId
     * @param num
     * @return
     */
    @RequestMapping("/addToCart")
    public String addToCart(RedirectAttributes attributes, @RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        CartItem cartItem = cartService.addToCart(skuId, num);
//        model.addAttribute("item", cartItem);
        attributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.glmall.com/addToCartSuccess.html";
    }

    /**
     * 跳转成功页面
     *
     * @param skuId
     * @return
     */
    @RequestMapping("/addToCartSuccess.html")
    public String addToCartSuccess(@RequestParam("skuId") Long skuId, Model model) {
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }

    @RequestMapping("/checkItem.html")
    public String checkItem(@RequestParam("skuId") Long skuId,
            @RequestParam("check") Integer check) {

        cartService.checkItem(skuId,check);
        return "redirect:http://cart.glmall.com/cartList.html";
    }

    @RequestMapping("/countItem.html")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num) {

        cartService.changeItemCount(skuId,num);
        return "redirect:http://cart.glmall.com/cartList.html";
    }

    @RequestMapping("/deleteItem.html")
    public String deleteItem(@RequestParam("skuId") Long skuId) {

        cartService.deleteItemCount(skuId);
        return "redirect:http://cart.glmall.com/cartList.html";
    }

    @GetMapping("/currentUserCartItemList")
    @ResponseBody
    public List<CartItem> getCurrentLoginUserCheckedCartItemList() {
        return cartService.getCurrentLoginUserCheckedCartItemList();
    }


}
