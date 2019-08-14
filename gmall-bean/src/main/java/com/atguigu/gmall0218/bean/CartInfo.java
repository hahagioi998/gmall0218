package com.atguigu.gmall0218.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author qiyu
 * @create 2019-08-03 17:55
 * @Description:购物车实体类
 */
@Data
public class CartInfo implements Serializable {
    //主键，商品编号
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    String id;

    //用户Id
    @Column
    String userId;

    //商品skuId
    @Column
    String skuId;

//    放入购物车的价格
    @Column
    BigDecimal cartPrice;

//    数量
    @Column
    Integer skuNum;
    //商品图片
    @Column
    String imgUrl;
    //商品名称sku
    @Column
    String skuName;

    // 实时价格
    @Transient
    BigDecimal skuPrice;
    // 下订单的时候，商品是否勾选
    @Transient
    String isChecked="0";
}
