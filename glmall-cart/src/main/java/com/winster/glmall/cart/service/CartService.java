package com.winster.glmall.cart.service;

import com.winster.glmall.cart.vo.Cart;
import com.winster.glmall.cart.vo.CartItem;

import java.util.List;

public interface CartService {
    /**
     * 将商品添加到购物车
     * @param skuId
     * @param num
     * @return
     */
    CartItem addToCart(Long skuId, Integer num);

    /**
     * 添加购物项到购物车
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);

    /**
     * 获取购物车数据
     * @return
     */
    Cart getCartList();

    /**
     * 清空购物车
     * @param cartKey
     */
    void clearCart(String cartKey);

    /**
     * 勾选购物项
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);

    void changeItemCount(Long skuId, Integer num);

    void deleteItemCount(Long skuId);

    List<CartItem> getCurrentLoginUserCheckedCartItemList();
}
