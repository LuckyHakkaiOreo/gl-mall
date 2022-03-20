package com.winster.glmall.cart.vo;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 需要计算的属性，必须重写其get方法
 */
public class Cart {
    private List<CartItem> items;

    private Integer countNum;// 商品总数
    private Integer countType;// 商品类型总数

    private BigDecimal totalAmount;// 总价
    private BigDecimal reduce;// 减免价格

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        Integer count = 0;
        if (!CollectionUtils.isEmpty(items)) {
            count = items.stream().map(CartItem::getCount).reduce(Integer::sum).get();
        }
        this.countNum = count;
        return count;
    }

    public void setCountNum(Integer countNum) {
        this.countNum = countNum;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal total = new BigDecimal(0);
        if (!CollectionUtils.isEmpty(items)) {
            // 被选中的商品才计算总价
            total = items.stream().filter(item -> item.getCheck())
                    .map(item -> item.getTotalPrice())
                    .reduce(BigDecimal::add).get();
        }
        this.totalAmount = total;
        return total;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }

    public Integer getCountType() {
        Integer count = 0;
        if (!CollectionUtils.isEmpty(items)) {
            count = items.size();
        }
        this.countType = count;
        return count;
    }

    public void setCountType(Integer countType) {
        this.countType = countType;
    }
}
