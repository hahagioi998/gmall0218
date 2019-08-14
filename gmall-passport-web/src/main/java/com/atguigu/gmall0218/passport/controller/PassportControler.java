package com.atguigu.gmall0218.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0218.bean.UserInfo;
import com.atguigu.gmall0218.passport.utils.JwtUtil;
import com.atguigu.gmall0218.service.UserInfoService;
import com.atguigu.gmall0218.service.UserService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author qiyu
 * @create 2019-07-31 23:06
 * @Description:单点登录控制器
 */
@Controller
public class PassportControler {


    @Value("${token.key}")
    String signKey;

    @Reference
    private UserService userService;

    @RequestMapping("index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        // 保存
        request.setAttribute("originUrl",originUrl);
        return "index";
    }

    /**
     * 登录校验
     * @return
     */
    @RequestMapping("login")
    @ResponseBody
    public String login(HttpServletRequest request, UserInfo userInfo){
        // 取得ip地址
        String ipAddr = request.getHeader("X-forwarded-for");
        if(userInfo !=null){
            UserInfo loginUser  = userService.login(userInfo);

            if(loginUser == null){//数据库中没有查询出来
                return "fail";

            }else {//从数据库中查询出来的
                //生成token
                Map map = new HashMap();
                map.put("userId",loginUser.getId());
                map.put("nickName",loginUser.getNickName());
                String token = JwtUtil.encode(signKey, map, ipAddr);

                return token;

            }

        }

        return "fail";
    }

    /**
     * 其他页面校验用户是否已经在redis中
     * @return
     */
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        String token = request.getParameter("token");
        String currentIp = request.getParameter("currentIp");

        //检查token


        Map<String, Object> map = JwtUtil.decode(token, signKey, currentIp);
        if(map != null){
            //检查redis中的信息
            String userId = (String)map.get("userId");
            UserInfo userInfo = userService.verify(userId);
            if(userInfo !=null){
                return "success";
            }
        }


        return "fail";

    }

}
