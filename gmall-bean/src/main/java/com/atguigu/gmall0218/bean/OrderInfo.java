package com.atguigu.gmall0218.bean;

import com.atguigu.gmall0218.bean.enums.OrderStatus;
import com.atguigu.gmall0218.bean.enums.PaymentWay;
import com.atguigu.gmall0218.bean.enums.ProcessStatus;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author qiyu
 * @create 2019-08-04 19:36
 * @Description:订单表
 */
@Data
public class OrderInfo implements Serializable {
    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String consignee;//收货人名称。页面获取

    @Column
    private String consigneeTel;//收货人电话。页面获取


    @Column
    private BigDecimal totalAmount;//总金额。计算

    @Column
    private OrderStatus orderStatus;//订单状态，用于显示给用户查看。设定初始值。

    @Column
    private ProcessStatus processStatus;//订单进度状态，程序控制、 后台管理查看。设定初始值，


    @Column
    private String userId;//用户Id。从拦截器已放到请求属性中。

    @Column
    private PaymentWay paymentWay;//支付方式（网上支付、货到付款）。页面获取

    @Column
    private Date expireTime;//默认当前时间+1天

    @Column
    private String deliveryAddress;//收货地址。页面获取

    @Column
    private String orderComment;//订单状态。页面获取

    @Column
    private Date createTime;//创建时间。设当前时间

    @Column
    private String parentOrderId;//拆单时产生，默认为空

    @Column
    private String trackingNo;//物流编号,初始为空，发货后补充


    @Transient
    private List<OrderDetail> orderDetailList;


    @Transient
    private String wareId;//库存

    @Column
    private String outTradeNo;//第三方支付编号

    /**
     * 计算总价格
     */
    public void sumTotalAmount() {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OrderDetail orderDetail : orderDetailList) {
            totalAmount = totalAmount.add(orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum())));
        }
        this.totalAmount = totalAmount;
    }

    public String getTradeBody(){
        OrderDetail orderDetail = orderDetailList.get(0);
        String tradeBody=orderDetail.getSkuName()+"等"+orderDetailList.size()+"件商品";
        return tradeBody;
    }
}
