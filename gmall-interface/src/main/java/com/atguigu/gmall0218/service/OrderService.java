package com.atguigu.gmall0218.service;

import com.atguigu.gmall0218.bean.OrderInfo;
import com.atguigu.gmall0218.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {
    /**
     * 添加订单
     * @param orderInfo
     * @return
     */
    public  String  saveOrder(OrderInfo orderInfo);

    /**
     * 生成流水号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 验证流水号
     * @param userId
     * @param tradeCodeNo
     * @return
     */
    boolean checkTradeCode(String userId,String tradeCodeNo);

    /**
     * 删除流水号
     * @param userId
     */
    void  delTradeCode(String userId);

    /**
     * 验证库存数量
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(String skuId, Integer skuNum);

    /**
     * 根据订单号查询订单
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(String orderId);

    /**
     * 取出来消息队列的消息，进行修改订单状态
     * @param orderId
     * @param paid
     */
    void updateOrderStatus(String orderId, ProcessStatus paid);

    /**
     * 发送订单状态通知减库存
     * @param orderId
     */
    void sendOrderStatus(String orderId);

    /**
     * 扫描过期订单的方法
     * @return
     */
    List<OrderInfo> getExpiredOrderList();

    /**
     * 处理未完成订单
     * @param orderInfo
     */
    void execExpiredOrder(OrderInfo orderInfo);

    /**
     * 返回一个新生成的子订单列表
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    List<OrderInfo> orderSplit(String orderId, String wareSkuMap);

    /**
     * 将查询出来的OrderInfo对象编程json串
     * @param orderInfo
     * @return
     */
    Map initWareOrder(OrderInfo orderInfo);
}
