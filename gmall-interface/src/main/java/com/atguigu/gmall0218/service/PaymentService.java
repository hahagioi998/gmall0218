package com.atguigu.gmall0218.service;

import com.atguigu.gmall0218.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {

    /**
     * 保存支付信息
     * @param paymentInfo
     */
    void savePaymentInfo(PaymentInfo paymentInfo);

    /**
     * 根据编号查询订单
     * @param paymentInfoQuery
     * @return
     */
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    /**
     * 支付宝异步回调信息表示成功，然后更改交易记录的状态
     * @param out_trade_no
     * @param paymentInfoUPD
     */
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUPD);

    /**
     * 退款
     * @param orderId
     * @return
     */
    boolean refund(String orderId);

    /**
     * 微信支付
     * @param s
     * @param s1
     * @return
     */
    Map createNative(String s, String s1);


    /**
     * 支付模块利用消息队列通知订单系统，支付成功
     * @param paymentInfo
     * @param result
     */
    void sendPaymentResult(PaymentInfo paymentInfo,String result);

    /**
     * 向支付宝查询订单状态
     * @param paymentInfoQuery
     * @return
     */
    boolean checkPayment(PaymentInfo paymentInfoQuery);

    /**
     * 利用延迟队列反复调用查询接口
     * @param outTradeNo
     * @param delaySec
     * @param checkCount
     */
    void sendDelayPaymentResult(String outTradeNo,int delaySec ,int checkCount);

    /**
     * 将订单状态改为关闭
     * @param orderId
     */
    void closePayment(String orderId);
}
