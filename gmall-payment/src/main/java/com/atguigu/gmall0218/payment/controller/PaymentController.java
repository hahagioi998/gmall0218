package com.atguigu.gmall0218.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall0218.bean.OrderInfo;
import com.atguigu.gmall0218.bean.PaymentInfo;
import com.atguigu.gmall0218.bean.enums.PaymentStatus;
import com.atguigu.gmall0218.handler.LoginRequire;
import com.atguigu.gmall0218.payment.config.AlipayConfig;
import com.atguigu.gmall0218.service.OrderService;
import com.atguigu.gmall0218.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author qiyu
 * @create 2019-08-05 21:25
 * @Description:支付控制器
 */
@Controller
public class PaymentController {


    @Reference
    private OrderService orderService;

    @Reference
    PaymentService paymentService;

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    AlipayClient alipayClient;



    @LoginRequire
    @RequestMapping(value = "index")
    public String index(HttpServletRequest request, Model model){
        // 获取订单的id
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo =orderService.getOrderInfo(orderId);


        //返回订单号跟订单总金额
        model.addAttribute("orderId",orderId);
        model.addAttribute("totalAmount",orderInfo.getTotalAmount());
        return "index";
    }


    @RequestMapping(value = "/alipay/submit",method = RequestMethod.POST)
    @ResponseBody
    public String submitPayment(HttpServletRequest request, HttpServletResponse response){
        // 获取订单的id
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo =orderService.getOrderInfo(orderId);

        //保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());

        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("-----");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);

        //保存信息
        paymentService.savePaymentInfo(paymentInfo);

        //创建api对应的request
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        //回调地址
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        //公共地址
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);



        Map<String,Object> bizContnetMap = new HashMap<>();
        bizContnetMap.put("out_trade_no",paymentInfo.getOutTradeNo());
        bizContnetMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        bizContnetMap.put("subject",paymentInfo.getSubject());
        bizContnetMap.put("total_amount",paymentInfo.getTotalAmount());

        // 将map变成json
        String Json = JSON.toJSONString(bizContnetMap);
        alipayRequest.setBizContent(Json);

        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        response.setContentType("text/html;charset=UTF-8");

        // 调用延迟队列反复查询接口
        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),15,3);
        return form;
    }

    /**
     * 重定向到支付模块（同步回调）
     * @return
     */
    @RequestMapping(value = "/alipay/callback/return",method = RequestMethod.GET)
    public String callbackReturn(){
        return "redirect:"+AlipayConfig.return_order_url;
    }


    /**
     * 支付宝异步回调
     * @param paramMap
     * @param request
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping(value = "/alipay/callback/notify",method = RequestMethod.POST)
    public String paymentNotify(@RequestParam Map<String,String> paramMap, HttpServletRequest request) throws AlipayApiException {
        /**
         * 1、验证回调信息的真伪
             2、验证用户付款的成功与否
             3、把新的支付状态写入支付信息表{paymentInfo}中。
             4、通知电商
             5、给支付宝返回回执。
         */

        // Map<String, String> paramsMap = ... //将异步通知中收到的所有参数都存放到map中
        boolean flag = false; //调用SDK验证签名
        try {
            flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if(flag){
        //验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
        // 对业务的二次校验
        // 只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功。
        // 支付成功之后，需要做什么？
        // 需要得到trade_status
            String trade_status = paramMap.get("trade_status");
            // 通过out_trade_no 查询支付状态记录
            String out_trade_no = paramMap.get("out_trade_no");

        //String total_amount = paramMap.get("total_amount");   总金额也可以验证，业务逻辑是一样的
            if("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
                //先去数据库查询一下订单状态
                PaymentInfo paymentInfoQuery = new PaymentInfo();
                paymentInfoQuery.setOutTradeNo(out_trade_no);
                PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);

                // 当前的订单支付状态如果是已经付款，或者是关闭
                if(paymentInfo.getPaymentStatus()==PaymentStatus.PAID || paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED){
                    return "failure";
                }

                // 更新交易记录的状态！

                PaymentInfo paymentInfoUPD = new PaymentInfo();
                paymentInfoUPD.setPaymentStatus(PaymentStatus.PAID);
                paymentInfoUPD.setCallbackTime(new Date());

                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUPD);
                //发送通知给订单状态
                paymentService.sendPaymentResult(paymentInfo,"success");

                return "success";
            }


        }else {//验签失败
            //  验签失败则记录异常日志，并在response中返回failure.
            return "failure";

        }
        return "failure";


    }
    // 发送验证
    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,@RequestParam("result") String result){
        paymentService.sendPaymentResult(paymentInfo,result);
        return "sent payment result";
    }

    // http://payment.gmall.com/refund?orderId=100
    // 退款
    @RequestMapping("refund")
    @ResponseBody
    public String refund(String orderId){
        // 退款接口
        boolean result =  paymentService.refund(orderId);

        return ""+result;
    }


    /**
     * 微信的
     * @param orderId
     * @return
     */
    @RequestMapping("wx/submit")
    @ResponseBody
    public Map createNative(String orderId){
    // 做一个判断：支付日志中的订单支付状态 如果是已支付，则不生成二维码直接重定向到消息提示页面！
    // 调用服务层数据
    // 第一个参数是订单Id ，第二个参数是多少钱，单位是分
            String orderIds = UUID.randomUUID().toString().replace("-","");
            Map map = paymentService.createNative(orderIds +"", "1");
            System.out.println(map.get("code_url"));
            // data = map
            return map;
    }


    // 查询订单信息
    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String queryPaymentResult(String orderId){

        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOrderId(orderId);
        boolean flag = paymentService.checkPayment(paymentInfoQuery);
        return ""+flag;
    }

}
