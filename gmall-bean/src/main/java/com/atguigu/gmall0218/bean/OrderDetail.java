package com.atguigu.gmall0218.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author qiyu
 * @create 2019-08-04 19:42
 * @Description:订单明细
 */
@Data
public class OrderDetail implements Serializable {
    @Id
    @Column
    private String id;
    @Column
    private String orderId;//订单编号，主表保存后给从表
    @Column
    private String skuId;//商品id 页面传递
    @Column
    private String skuName;//商品名称，后台添加
    @Column
    private String imgUrl;//图片路径，后台添加
    @Column
    private BigDecimal orderPrice;//商品单价，从页面中获取，并验价。
    @Column
    private Integer skuNum;//商品个数，从页面中获取

    @Transient
    private String hasStock;//是否有库存的标志如果商品在库存中有足够数据，suceess = “1”，fail=“0”
}
