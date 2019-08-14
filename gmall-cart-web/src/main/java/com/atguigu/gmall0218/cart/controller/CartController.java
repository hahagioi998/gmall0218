package com.atguigu.gmall0218.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0218.bean.CartInfo;
import com.atguigu.gmall0218.bean.SkuInfo;
import com.atguigu.gmall0218.cart.handler.CartCookieHandler;
import com.atguigu.gmall0218.handler.LoginRequire;
import com.atguigu.gmall0218.service.CartService;
import com.atguigu.gmall0218.service.ItemManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author qiyu
 * @create 2019-08-03 18:28
 * @Description:购物车控制器
 */
@Controller
public class CartController {
//1、获得参数：skuId 、skuNum
//2、判断该用户是否登录，用userId判断
//3、如果登录则调用后台的service的业务方法
//4、如果未登录，要把购物车信息暂存到cookie中。
//    实现利用cookie保存购物车的方法。

    @Reference
    private CartService cartService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    @Reference
    private ItemManageService itemManageService;

    /**
     * 添加商品到购物车
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "addToCart",method = RequestMethod.POST)
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response) {
        // 获取userId，skuId，skuNum
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");
        String userId = (String) request.getAttribute("userId");
        // 判断用户是否登录
        if(userId != null){//用户已经登录
            cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else {//用户未登录，将商品信息添加到cookie中的购物车中
            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));

        }


        // 取得sku信息对象放到域中
        SkuInfo skuInfo = itemManageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
       // request.setAttribute("userId",userId);

        return "success";
    }

    /**
     * 显示购物车中的信息
     * 合并思路：
     *
     *      把cookie中的购物车合并进来，同时把cookie中的清空
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public  String cartList(HttpServletRequest request,HttpServletResponse response){
//        2、如果用户已登录从缓存中取值，如果缓存没有，加载数据库。
//        3、如果用户未登录从cookie中取值。
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartList = null;
        if(userId != null){
            // 合并购物车
            //从cookie中查找购物车
            List<CartInfo> cartListFromCookie  = cartCookieHandler.getCartList(request);
            if(cartListFromCookie != null && cartListFromCookie.size() >0){
                // 开始合并
                cartList = cartService.mergeToCartList(cartListFromCookie,userId);
                // 删除cookie中的购物车
                cartCookieHandler.deleteCartCookie(request,response);

            }else {
                // 从redis中取得，或者从数据库中
                cartList = cartService.getCartList(userId);
            }



        }else {
            //从cookie中取得列表
            cartList = cartCookieHandler.getCartList(request);

        }

        request.setAttribute("cartList",cartList);
        System.out.println("*************cartList:"+cartList);
        return "cartList";

    }

    /**
     * 选中状态的变更
     * @param request
     * @param response
     */
    @RequestMapping(value = "checkCart",method = RequestMethod.POST)
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        String userId=(String) request.getAttribute("userId");
        if (userId!=null){//修改缓存中的数据
            cartService.checkCart(skuId,isChecked,userId);
        }else{//修改cookie中的数据
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }

    }

    /**
     * 点击结算按钮，强制登录然后跳转
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "toTrade",method = RequestMethod.GET)
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cookieHandlerCartList  = cartCookieHandler.getCartList(request);
        if(cookieHandlerCartList != null&& cookieHandlerCartList.size()>0){
            cartService.mergeToCartList(cookieHandlerCartList,userId);
            cartCookieHandler.deleteCartCookie(request,response);
        }
        return "redirect://order.gmall.com/trade";
    }


}
