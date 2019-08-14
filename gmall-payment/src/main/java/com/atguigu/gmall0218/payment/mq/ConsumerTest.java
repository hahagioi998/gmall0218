package com.atguigu.gmall0218.payment.mq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.sound.midi.Soundbank;

/**
 * @author qiyu
 * @create 2019-08-09 18:14
 * @Description:mq消费端
 */
public class ConsumerTest {
    public static void main(String[] args) throws JMSException {

        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, "tcp://192.168.122.134:61616");

        Connection connection = activeMQConnectionFactory.createConnection();

        connection.start();

        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

        Queue queue = session.createQueue("atguigu-true");

        MessageConsumer consumer = session.createConsumer(queue);


        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if(message instanceof TextMessage){
                    try {
                        String text = ((TextMessage) message).getText();
                        System.out.println("*****************"+text);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }

                }
            }
        });


    }
}
