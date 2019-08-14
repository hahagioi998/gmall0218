package com.atguigu.gmall0218.order.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0218.bean.OrderDetail;
import com.atguigu.gmall0218.bean.OrderInfo;
import com.atguigu.gmall0218.bean.enums.ProcessStatus;
import com.atguigu.gmall0218.config.ActiveMQConfig;
import com.atguigu.gmall0218.config.ActiveMQUtil;
import com.atguigu.gmall0218.config.RedisUtil;
import com.atguigu.gmall0218.order.mapper.OrderDetailMapper;
import com.atguigu.gmall0218.order.mapper.OrderInfoMapper;
import com.atguigu.gmall0218.service.OrderService;
import com.atguigu.gmall0218.service.PaymentService;
import com.atguigu.gmall0218.util.HttpClientUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

/**
 * @author qiyu
 * @create 2019-08-04 20:46
 * @Description:
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;


    @Reference
    private OrderService orderService;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Reference
    private PaymentService paymentService;

    /**
     * 添加订单
     * @param orderInfo
     * @return
     */
    @Transactional
    @Override
    public String saveOrder(OrderInfo orderInfo) {
        // 设置创建时间
        orderInfo.setCreateTime(new Date());

        // 设置失效时间
        //获取当前日历
        Calendar calendar  = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);

        //设置当前时间加1
        orderInfo.setExpireTime(calendar.getTime());

        // 生成第三方支付编号
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);

        orderInfoMapper.insertSelective(orderInfo);

        String orderId = orderInfo.getId();
        // 插入订单详细信息
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderId);
            orderDetailMapper.insertSelective(orderDetail);
        }


        // 为了跳转到支付页面使用。支付会根据订单id进行支付。

        return orderId;
    }

    // 生成流水号
    public  String getTradeNo(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey,10*60,tradeCode);
        jedis.close();
        return tradeCode;
    }

    // 验证流水号
    public  boolean checkTradeCode(String userId,String tradeCodeNo){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        jedis.close();
        if (tradeCode!=null && tradeCode.equals(tradeCodeNo)){
            return  true;
        }else{
            return false;
        }
    }
    // 删除流水号
    public void  delTradeCode(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey =  "user:"+userId+":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }

    /**
     * 验证库存
     * @param skuId
     * @param skuNum
     * @return
     */
    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        if ("1".equals(result)){
            return  true;
        }else {
            return  false;
        }
    }

    /**
     * 根据订单号查询订单信息
     * @param orderId
     * @return
     */
    @Override
    public OrderInfo getOrderInfo(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);

        //将orderDetail放进orderInfo中
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        orderInfo.setOrderDetailList(orderDetailMapper.select(orderDetail));
        return orderInfo;
    }

    /**
     * 修改订单状态
     * @param orderId
     * @param processStatus
     */
    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    /**
     * 发送订单状态减库存
     * @param orderId
     */
    @Override
    public void sendOrderStatus(String orderId) {
        Connection connection = activeMQUtil.getConnection();
        String orderInfoJson = initWareOrder(orderId);

        try {
            connection.start();

            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建队列
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
            //创建消息提供者
            MessageProducer producer = session.createProducer(order_result_queue);
            //创建消息对象
            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
            //orderInfo组成json字符串
            activeMQTextMessage.setText(orderInfoJson);
            //发送消息
            producer.send(activeMQTextMessage);
            //提交
            session.commit();
            //关闭
            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

    /**
     * 扫描过期的订单
     * @return
     */
    @Override
    public List<OrderInfo> getExpiredOrderList() {
        // 当前系统时间>过期时间 and 当前状态是未支付！
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("processStatus",ProcessStatus.UNPAID).andLessThan("expireTime",new Date());
        List<OrderInfo> orderInfoList = orderInfoMapper.selectByExample(example);
        return orderInfoList;
    }

    /**
     * 处理未完成的订单
     * @param orderInfo
     */
    @Override
    @Async
    public void execExpiredOrder(OrderInfo orderInfo) {
        // 将订单状态改为关闭
        updateOrderStatus(orderInfo.getId(),ProcessStatus.CLOSED);
        // 关闭paymentInfo
        paymentService.closePayment(orderInfo.getId());
    }

    /**
     * 进行拆单
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    @Override
    public List<OrderInfo> orderSplit(String orderId, String wareSkuMap) {

        List<OrderInfo> subOrderInfoList = new ArrayList<>();
         /*
            1.  获取原始订单
            2.  将wareSkuMap 转换为我们能操作的对象
            3.  创建新的子订单
            4.  给子订单赋值，并保存到数据库
            5.  将子订单添加到集合中
            6.  更新原始订单状态！

          */

        OrderInfo orderInfoOrigin = getOrderInfo(orderId);
        // wareSkuMap [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
        List<Map> maps = JSON.parseArray(wareSkuMap, Map.class);
        if(maps!=null){
            //循环遍历集合
            for (Map map : maps) {
                //获取仓库Id
                String wareId = (String)map.get("wareId");
                //获取商品Id
                List<String> skuIds = (List<String>) map.get("skuIds");
                OrderInfo subOrderInfo  = new OrderInfo();
                //属性拷贝
                BeanUtils.copyProperties(orderInfoOrigin,subOrderInfo);
                //id变为null
                subOrderInfo.setId(null);
                subOrderInfo.setWareId(wareId);
                subOrderInfo.setParentOrderId(orderId);

                //价格：获取到原始订单的明细
                List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();

                //声明一个新的子订单明细集合
                ArrayList<OrderDetail> subOrderDetailArrayList = new ArrayList<>();
                //原始订单明细Id
                for (OrderDetail orderDetail : orderDetailList) {
                    //仓库对应的商品id
                    for (String skuId : skuIds) {
                        if (skuId.equals(orderDetail.getSkuId())) {
                            orderDetail.setId(null);
                            subOrderDetailArrayList.add(orderDetail);
                        }

                    }
                }

                //将子订单集合放入子订单中
                subOrderInfo.setOrderDetailList(subOrderDetailArrayList);

                //计算价格
                subOrderInfo.sumTotalAmount();
                //保存到数据库
                saveOrder(subOrderInfo);
                //将新的子订单添加到集合中

                subOrderInfoList.add(subOrderInfo);
            }
        }
        updateOrderStatus(orderId,ProcessStatus.SPLIT);

        return subOrderInfoList;
    }


    /**
     *  根据orderId 将orderInfo 变为json 字符串
     * @param orderId
     * @return
     */
    public String initWareOrder(String orderId) {
        OrderInfo orderInfo = getOrderInfo(orderId);
        Map map = initWareOrder(orderInfo);

        return JSON.toJSONString(map);
    }

    /**
     * 将查询出来的OrderInfo对象编程json串
     * @param orderInfo
     * @return
     */
    public Map initWareOrder(OrderInfo orderInfo) {
        HashMap<String, Object> map = new HashMap<>();
        // 给map 的key 赋值！
        // 设置初始化仓库信息方法
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody","测试用例");
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
        map.put("wareId",orderInfo.getWareId());
        // map.put("wareId",orderInfo.getWareId()); 仓库Id
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        // 创建一个集合来存储map
        ArrayList<Map> arrayList = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetailList) {
            HashMap<String, Object> orderDetailMap = new HashMap<>();
            orderDetailMap.put("skuId",orderDetail.getSkuId());
            orderDetailMap.put("skuNum",orderDetail.getSkuNum());
            orderDetailMap.put("skuName",orderDetail.getSkuName());
            arrayList.add(orderDetailMap);
        }
        map.put("details",arrayList);

        return map;


    }


}
