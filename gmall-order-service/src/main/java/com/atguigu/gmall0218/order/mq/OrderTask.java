package com.atguigu.gmall0218.order.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0218.bean.OrderInfo;
import com.atguigu.gmall0218.service.OrderService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author qiyu
 * @create 2019-08-11 1:18
 * @Description:轮询扫描关闭长期没有付款的订单
 */
@Component
@EnableScheduling
public class OrderTask {
    @Reference
    private OrderService orderService;
    // 5 每分钟的第五秒
    // 0/5 没隔五秒执行一次
    @Scheduled(cron = "5 * * * * ?")
    public void  work(){
        System.out.println("Thread ====== "+ Thread.currentThread());
    }
    @Scheduled(cron = "0/5 * * * * ?")
    public void  work1(){
        System.out.println("Thread1 ====== "+ Thread.currentThread());
    }

    @Scheduled(cron = "0/20 * * * * ?")
    public void checkOrder(){
        /*
       1.	查询有多少订单是过期：
            什么样的订单算是过期了？
            当前系统时间>过期时间 and 当前状态是未支付！

        2.	循环过期订单列表，进行处理！
            orderInfo
            paymentInfo
        */
        List<OrderInfo> orderInfoList =  orderService.getExpiredOrderList();

        for (OrderInfo orderInfo : orderInfoList) {
            // 关闭过期订单
            orderService.execExpiredOrder(orderInfo);
        }
    }

}
