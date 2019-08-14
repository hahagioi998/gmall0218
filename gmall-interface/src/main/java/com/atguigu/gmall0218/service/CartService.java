package com.atguigu.gmall0218.service;

import com.atguigu.gmall0218.bean.CartInfo;

import java.util.List;

public interface CartService {
    /**
     * 添加商品到购物车
     * @param skuId
     * @param userId
     * @param skuNum
     */
     void  addToCart(String skuId,String userId,Integer skuNum);

    /**
     * 查询该用户的购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);
    /**
     * 合并购物车与cookie中的
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId);

    /**
     * 购物车中选中状态的变更
     * @param skuId
     * @param isChecked
     * @param userId
     */
    void checkCart(String skuId, String isChecked, String userId);

    /**
     * 查询选中的购物车列表
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);
}
