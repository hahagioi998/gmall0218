package com.atguigu.gmall0218.handler;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0218.util.HttpClientUtil;
import com.atguigu.gmall0218.utils.CookieUtil;
import com.atguigu.gmall0218.utils.WebConst;

import io.jsonwebtoken.impl.Base64UrlCodec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;
import java.util.Map;


/**
 * @author qiyu
 * @create 2019-08-02 19:13
 * @Description:登录成功后将token写进cookie中
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getParameter("newToken");
        //将token放进cookie中
        if(token != null){
            CookieUtil.setCookie(request,response,"token",token, WebConst.cookieMaxAge,false);
        }

        if(token==null){
            token = CookieUtil.getCookieValue(request, "token", false);
        }



        if(token !=null){
            Map map = getUserMapByToken(token);
            String nickName = (String)map.get("nickName");
            request.setAttribute("nickName",nickName);
        }


        HandlerMethod handlerMethod =(HandlerMethod) handler;
        LoginRequire loginRequireAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if(loginRequireAnnotation!=null){
            //获取校验的盐值
            String remoteAddr = request.getHeader("x-forwarded-for");
            String result = HttpClientUtil.doGet(WebConst.VERIFY_URL + "?token=" + token + "&currentIp=" + remoteAddr);

            if("success".equals(result)){
                Map map = getUserMapByToken(token);
                String userId =(String) map.get("userId");
                request.setAttribute("userId",userId);
                return true;
            }else{
                if(loginRequireAnnotation.autoRedirect()){
                    String  requestURL = request.getRequestURL().toString();
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    response.sendRedirect(WebConst.LOGIN_URL+"?originUrl="+encodeURL);
                    return false;
                }
            }
        }

        return true;


    }

    //将字符串token转换为Map存储
    private Map getUserMapByToken(String token) {

        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] tokenBytes = base64UrlCodec.decode(tokenUserInfo);
        String tokenJson = null;
        try {
            tokenJson = new String(tokenBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map map = JSON.parseObject(tokenJson, Map.class);
        return map;
    }
}
