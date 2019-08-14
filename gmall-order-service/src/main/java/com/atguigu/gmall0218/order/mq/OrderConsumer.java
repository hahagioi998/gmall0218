package com.atguigu.gmall0218.order.mq;

import com.atguigu.gmall0218.bean.enums.ProcessStatus;
import com.atguigu.gmall0218.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;


/**
 * @author qiyu
 * @create 2019-08-09 19:45
 * @Description:消费类
 */
@Component
public class OrderConsumer {

    @Autowired
    OrderService orderService;


    /**
     * destination 表示监听的队列名称
     */
    @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage) throws JMSException {
        // 通过mapMessage获取
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");

        if("success".equals(result)){

            //更新订单状态
            orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
            //发送消息给库存
            orderService.sendOrderStatus(orderId);
            //更新订单状态
            orderService.updateOrderStatus(orderId, ProcessStatus.NOTIFIED_WARE);
        }else {
            orderService.updateOrderStatus(orderId,ProcessStatus.UNPAID);
        }

    }

    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {

            // 通过mapMessage获取
            String orderId = mapMessage.getString("orderId");
            String status = mapMessage.getString("status");
            if ("DEDUCTED".equals(status)){
                orderService.updateOrderStatus(orderId, ProcessStatus.DELEVERED);
            }


    }




}
