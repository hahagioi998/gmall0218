package com.atguigu.gmall0218.payment.mq;

import com.atguigu.gmall0218.config.ActiveMQUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;

/**
 * @author qiyu
 * @create 2019-08-09 17:57
 * @Description:mq服务端
 */
@Component
public class ProducerTest {
    @Autowired
    private ActiveMQUtil activeMQUtil;

    public static void main(String[] args) throws JMSException {
        /*
        1.  创建连接工厂
        2.  创建连接
        3.  打开连接
        4.  创建session
        5.  创建队列
        6.  创建消息提供者
        7.  创建消息对象
        8.  发送消息
        9.  关闭
        */
       // Connection connection = new ProducerTest().activeMQUtil.getConnection();
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.122.134:61616");
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();

        // 第一个参数：是否开启事务
        // 第二个参数：表示开启/关闭事务的相应配置参数，
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);//必须提交

        Queue queue = session.createQueue("qqqqqq");
        MessageProducer producer = session.createProducer(queue);

        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        activeMQTextMessage.setText("下午什么也没听进去");

        producer.send(activeMQTextMessage);

        session.commit();

        session.close();
        producer.close();
        connection.close();


    }
}
