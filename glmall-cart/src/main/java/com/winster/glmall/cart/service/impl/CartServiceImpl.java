package com.winster.glmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.winster.common.constant.CartConstant;
import com.winster.common.to.SkuInfoTo;
import com.winster.common.utils.R;
import com.winster.glmall.cart.feign.ProductFeign;
import com.winster.glmall.cart.interceptor.CartInterceptor;
import com.winster.glmall.cart.service.CartService;
import com.winster.glmall.cart.vo.Cart;
import com.winster.glmall.cart.vo.CartItem;
import com.winster.glmall.cart.vo.UserInfoTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ProductFeign productFeign;

    @Override
    public CartItem addToCart(Long skuId, Integer num) {
        // 1、获取当前用户的购物车缓存
        BoundHashOperations cartCache = getCartCache();
        CartItem cartItem = new CartItem();

        // 如果购物车中已经存在这个商品，则无需再从远程服务中查询
        String result = (String) cartCache.get(skuId.toString());
        // 当前购物车已经保存过这个商品，仅需修改数量
        if (StringUtils.isNotBlank(result)) {
            cartItem = JSON.parseObject(result, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartCache.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }

        // TODO 其实这里可以异步查
        // 2、远程查询当前要添加的商品信息
        R skuInfo = productFeign.info(skuId);

        SkuInfoTo skuInfoTo = JSON.parseObject(JSON.toJSONString(skuInfo.get("skuInfo")), SkuInfoTo.class);
        cartItem.setSkuId(skuId);
        cartItem.setCheck(true);
        cartItem.setCount(num);
        cartItem.setImage(skuInfoTo.getSkuDefaultImg());
        cartItem.setTitle(skuInfoTo.getSkuTitle());
        cartItem.setPrice(skuInfoTo.getPrice());

        // 3、远程查询sku的组合信息
        List<String> aList = productFeign.getSkuSaleAttrValues(skuId);
        cartItem.setSkuAttr(aList);

        // 4、将购物项保存到redis中
        cartCache.put(skuId.toString(), JSON.toJSONString(cartItem));

        return cartItem;
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations cartCache = getCartCache();

        String result = (String) cartCache.get(skuId.toString());
        CartItem cartItem = new CartItem();
        // 当前购物车已经保存过这个商品，仅需修改数量
        if (StringUtils.isNotBlank(result)) {
            cartItem = JSON.parseObject(result, CartItem.class);
        }
        return cartItem;
    }

    @Override
    public Cart getCartList() {
        Cart cart = new Cart();
        // 1、判断用户是否已经登录
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            // 用户已登录，需要将未登录的购物车合并到已登录购物车
            cartKey = CartConstant.CART_CACHE_PREFIX + userInfoTo.getUserId();
        }
        // 2、获取临时购物车里边的购物项数据
        String tmpCartKey = CartConstant.CART_CACHE_PREFIX + userInfoTo.getUserKey();
        BoundHashOperations<String, Object, Object> tmpHash = stringRedisTemplate.boundHashOps(tmpCartKey);
        List<Object> tmpValues = tmpHash.values();
        List<CartItem> tmpItems = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tmpValues)) {
            tmpItems = tmpValues.stream().map(v -> JSON.parseObject((String) v, CartItem.class)).collect(Collectors.toList());
        }

        // 3、获取登录用户购物车里的购物项数据
        BoundHashOperations<String, Object, Object> hash = stringRedisTemplate.boundHashOps(cartKey);
        if (hash == null) {
            cart.setItems(tmpItems);
            return cart;
        }

        List<Object> values = hash.values();
        // 登录用户购物车没有数据
        if (CollectionUtils.isEmpty(values)) {
            cart.setItems(tmpItems);
            return cart;
        }
        // 4、合并登录购物车和临时购物车的购物项
        List<CartItem> items = values.stream().map(v -> JSON.parseObject((String) v, CartItem.class)).collect(Collectors.toList());
        tmpItems.forEach(ti -> {
            AtomicReference<Boolean> exist = new AtomicReference<>(false);
            items.forEach(i -> {
                if (i.getSkuId().equals(ti.getSkuId())) {
                    i.setCount(i.getCount() + ti.getCount());
                    exist.set(true);
                    // 保存合并后的购物车数据
                    hash.put(i.getSkuId().toString(), JSON.toJSONString(i));
                }
            });
            if (!exist.get()) {
                items.add(ti);
                hash.put(ti.getSkuId().toString(), JSON.toJSONString(ti));
            }
        });
        cart.setItems(items);

        // 清空临时购物车
        clearCart(tmpCartKey);
        return cart;
    }

    @Override
    public void clearCart(String cartKey) {
        stringRedisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations cartCache = getCartCache();
        CartItem cartItem = JSON.parseObject((String) cartCache.get("" + skuId), CartItem.class);
        cartItem.setCheck(check == 1);
        cartCache.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        BoundHashOperations cartCache = getCartCache();
        CartItem cartItem = JSON.parseObject((String) cartCache.get("" + skuId), CartItem.class);
        cartItem.setCount(num);
        cartCache.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItemCount(Long skuId) {
        BoundHashOperations cartCache = getCartCache();
        cartCache.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getCurrentLoginUserCheckedCartItemList() {
        // 获取当前用户信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null) {
            return null;
        }
        // 用户已登录，获取购物车key
        String cartKey = CartConstant.CART_CACHE_PREFIX + userInfoTo.getUserId();

        // 获取登录用户购物车里的购物项数据
        BoundHashOperations<String, Object, Object> hash = stringRedisTemplate.boundHashOps(cartKey);
        if (hash == null) {
            return null;
        }
        List<Object> values = hash.values();
        List<CartItem> items = values.stream().map(v -> JSON.parseObject((String) v, CartItem.class))
                .filter(CartItem::getCheck)
                .collect(Collectors.toList());
        // 更新购物车里商品的价格为当前最新的价格
        List<Long> skuIds = items.stream().map(CartItem::getSkuId).collect(Collectors.toList());
        List<SkuInfoTo> skuInfoList = productFeign.getSkuInfoListByIds(skuIds);
        items.forEach(item->{
            Optional<SkuInfoTo> any = skuInfoList.stream().filter(info -> item.getSkuId().equals(info.getSkuId())).findAny();
            if (any.isPresent()) {
                item.setPrice(any.get().getPrice());
            }
        });

        return items;
    }

    /**
     * 获取到要操作的购物车
     *
     * @return
     */
    private BoundHashOperations getCartCache() {
        //1、获取当前用户信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            // 用户已登录
            cartKey = CartConstant.CART_CACHE_PREFIX + userInfoTo.getUserId();
        } else {
            // 用户未登录
            cartKey = CartConstant.CART_CACHE_PREFIX + userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> hash = stringRedisTemplate.boundHashOps(cartKey);

        return hash;
    }
}
