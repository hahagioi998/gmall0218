package com.atguigu.gmall0218.payment.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmall0218.bean.OrderInfo;
import com.atguigu.gmall0218.bean.PaymentInfo;
import com.atguigu.gmall0218.bean.enums.PaymentStatus;
import com.atguigu.gmall0218.config.ActiveMQUtil;
import com.atguigu.gmall0218.config.HttpClient;
import com.atguigu.gmall0218.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall0218.service.OrderService;
import com.atguigu.gmall0218.service.PaymentService;
import com.github.wxpay.sdk.WXPayUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qiyu
 * @create 2019-08-05 23:23
 * @Description:
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    // 服务号Id
    @Value("${appid}")
    private String appid;
    // 商户号Id
    @Value("${partner}")
    private String partner;
    // 密钥
    @Value("${partnerkey}")
    private String partnerkey;


    @Autowired
    PaymentInfoMapper paymentInfoMapper;


    @Reference
    private OrderService orderService;

    @Autowired
    private AlipayClient alipayClient;


    @Autowired
    private ActiveMQUtil activeMQUtil;

    /**
     * 保存支付信息
     * @param paymentInfo
     */
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    /**
     * 根据编号查询出来订单状态
     * @param paymentInfoQuery
     * @return
     */
    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery) {

        return paymentInfoMapper.selectOne(paymentInfoQuery);
    }

    /**
     * 更改订单状态
     * @param out_trade_no
     * @param paymentInfo
     */
    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }

    /**
     * 退款
     * @param orderId
     * @return
     */
    @Override
    public boolean refund(String orderId) {
        // 通过orderId 获取数据
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("refund_amount",orderInfo.getTotalAmount());
        map.put("refund_reason","不买了！");


        request.setBizContent(JSON.toJSONString(map));


        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            // 更新状态！
            System.out.println("调用成功");
            return true;

        } else {
            System.out.println("调用失败");
            return false;
        }
    }

    /**.
     * 微信支付
     * @param orderId
     * @param money
     * @return
     */
    @Override
    public Map createNative(String orderId, String money) {
        //1.创建参数
        Map<String,String> param=new HashMap();//创建参数
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "尚硅谷");//商品描述
        param.put("out_trade_no", orderId);//商户订单号
        param.put("total_fee",money);//总金额（分）
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", "http://www.weixin.qq.com/wxpay/pay.php");//回调地址(随便写)
        param.put("trade_type", "NATIVE");//交易类型
        try {
            //2.生成要发送的xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(xmlParam);
            HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            //3.获得结果
            String result = client.getContent();
            System.out.println(result);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            Map<String, String> map=new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));//支付地址
            map.put("total_fee", money);//总金额
            map.put("out_trade_no",orderId);//订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();

        }

        return new HashMap<>();
    }

    /**
     * 支付模块利用消息队列通知订单系统，支付成功
     * @param paymentInfo
     * @param result
     */
    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {
        Connection connection = activeMQUtil.getConnection();

        try {//打开连接

            connection.start();
            // 创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建队列
            Queue payment_result_queue = session.createQueue("PAYMENT_RESULT_QUEUE");
            // 创建消息提供者
            MessageProducer producer = session.createProducer(payment_result_queue);
            // 创建消息对象
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("orderId",paymentInfo.getOrderId());
            activeMQMapMessage.setString("result",result);

            // 发送消息

            producer.send(activeMQMapMessage);
            // 提交
            session.commit();

            //关闭
            closeProDucer(connection, session, producer);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    /**
     * 向支付宝查询订单状态
     * 查询支付是否成功
     * @param paymentInfoQuery
     * @return
     */
    @Override
    public boolean checkPayment(PaymentInfo paymentInfoQuery) {
        PaymentInfo paymentInfo = getPaymentInfo(paymentInfoQuery);
        // 查询当前的支付信息
        if(paymentInfo.getPaymentStatus()== PaymentStatus.PAID||
                paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED){
            return true;
        }


        //支付宝支付信息
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, String> queryMap = new HashMap<>();

        queryMap.put("out_trade_no",paymentInfo.getOutTradeNo());

        request.setBizContent(JSON.toJSONString(queryMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //判断支付宝返回的结果
        if(response.isSuccess()){
            if ("TRADE_SUCCESS".equals(response.getTradeStatus())||
                    "TRADE_FINISHED".equals(response.getTradeStatus())){
                //支付成功
                //修改数据库支付状态
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                updatePaymentInfo(paymentInfo.getOutTradeNo(),paymentInfoUpd);

                //通知订单支付完成
                sendPaymentResult(paymentInfo,"success");
                return true;
            }else {
                System.out.println("支付失败");
                return false;
            }

        }else {
            System.out.println("支付失败");
            return false;
        }

    }

    /**
     * 利用延迟队列反复调用查询接口
     * @param outTradeNo
     * @param delaySec
     * @param checkCount
     */
    @Override
    public void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount) {
        //创建工厂
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();
            //创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue payment_result_check_queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payment_result_check_queue);
            //创建消息对象
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("outTradeNo",outTradeNo);
            activeMQMapMessage.setInt("delaySec",delaySec);
            activeMQMapMessage.setInt("checkCount",checkCount);

            //设置延迟队列的开启
            activeMQMapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*1000);

            producer.send(activeMQMapMessage);


            //提交
            session.commit();

            //关闭
            closeProDucer(connection,session,producer);
        } catch (JMSException e) {
            e.printStackTrace();
        }






    }
    // 关闭支付信息
    @Override
    public void closePayment(String orderId) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderId",orderId);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }

    /**
     * 关闭消息队列
     * @param connection
     * @param session
     * @param producer
     * @throws JMSException
     */
    private void closeProDucer(Connection connection, Session session, MessageProducer producer) throws JMSException {
        producer.close();
        session.close();
        connection.close();
    }


}
