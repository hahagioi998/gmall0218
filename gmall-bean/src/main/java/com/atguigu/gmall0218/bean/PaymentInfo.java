package com.atguigu.gmall0218.bean;

import com.atguigu.gmall0218.bean.enums.PaymentStatus;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author qiyu
 * @create 2019-08-05 22:45
 * @Description:支付信息表
 */
@Data
public class PaymentInfo implements Serializable {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String  id;

    @Column
    private String outTradeNo;//订单中已生成的对外交易编号。订单中获取

    @Column
    private String orderId;//订单编号

    @Column
    private String alipayTradeNo;//支付宝交易编号  初始为空，支付宝回调时生成

    @Column
    private BigDecimal totalAmount;//订单金额。订单中获取

    @Column
    private String Subject;//交易内容。利用商品名称拼接。

    @Column
    private PaymentStatus paymentStatus;//支付状态，默认值未支付。

    @Column
    private Date createTime;

    @Column
    private Date callbackTime;//回调时间，初始为空，支付宝异步回调时记录

    @Column
    private String callbackContent;//回调信息，初始为空，支付宝异步回调时记录
}
