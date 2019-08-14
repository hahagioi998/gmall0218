package com.atguigu.gmall0218.cart.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.atguigu.gmall0218.bean.CartInfo;
import com.atguigu.gmall0218.bean.SkuInfo;
import com.atguigu.gmall0218.service.ItemManageService;
import com.atguigu.gmall0218.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qiyu
 * @create 2019-08-03 18:31
 * @Description:利用cookie保存购物车
 */
@Component
public class CartCookieHandler {
//1、先查询出来在cookie中的购物车，反序列化成列表。
//2、通过循环比较有没有该商品
//3、如果有，增加数量
//4、如果没有，增加商品
//5、然后把列表反序列化，利用之前最好的CookieUtil保存到cookie中。


    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;

    @Reference
    private ItemManageService itemManageService;


    // 未登录的时候，添加到购物车
    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, Integer skuNum){
        //判断cookie中是否有购物车 有可能有中文，所有要进行序列化
        String cartJson  = CookieUtil.getCookieValue(request, cookieCartName, true);

        List<CartInfo> cartInfoList = new ArrayList<>();
        boolean ifExist=false;

        if(cartJson !=null && cartJson.length() >0){
            cartInfoList = JSON.parseArray(cartJson,CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {

                if(cartInfo.getSkuId().equals(skuId)){//购物车存在该商品
                    //设置数量
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                    //设置价格
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());

                    ifExist = true;


                }

            }

        }


        // //购物车里没有对应的商品 或者 没有购物车
        if(!ifExist){
            //把商品信息取出来，新增到购物车
            SkuInfo skuInfo = itemManageService.getSkuInfo(skuId);
            CartInfo cartInfo = new CartInfo();

            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);
            cartInfoList.add(cartInfo);
        }



        // 把购物车写入cookie
        String newCartJson = JSON.toJSONString(cartInfoList);
        CookieUtil.setCookie(request,response,cookieCartName,newCartJson,COOKIE_CART_MAXAGE,true);

    }

    /**
     * 用户未登录，查询cookie 中购物车列表
     * @param request
     * @return
     */
    public List<CartInfo> getCartList(HttpServletRequest request) {
        String cartJson = CookieUtil.getCookieValue(request, cookieCartName, true);
        if(StringUtils.isNotEmpty(cartJson)){
            List<CartInfo> cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
            return cartInfoList;
        }

        return null;


    }

    /**
     * 删除cookie中的购物车
     *
     * @param request
     * @param response
     */
    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request,response,cookieCartName);
    }

    /**
     * 修改cookie中的isChecked值
     * @param request
     * @param response
     * @param skuId
     * @param isChecked
     */
    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        //  取出购物车中的商品
        List<CartInfo> cartList = getCartList(request);
        if(cartList !=null && cartList.size()>0){
            for (CartInfo cartInfo : cartList) {
                if (cartInfo.getSkuId().equals(skuId)) {
                    cartInfo.setIsChecked(isChecked);
                }
            }
        }


        //重新保存到cookie中保存
        String newCartJson = JSON.toJSONString(cartList);
        CookieUtil.setCookie(request,response,cookieCartName,newCartJson,COOKIE_CART_MAXAGE,true);
    }
}
