package com.atguigu.gmall0218.user.controller;

import com.atguigu.gmall0218.bean.UserAddress;
import com.atguigu.gmall0218.bean.UserInfo;
import com.atguigu.gmall0218.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author qiyu
 * @create 2019-07-23 12:55
 * @Description:TODO(这里用一句话来描述这个类的作用)
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;



    @RequestMapping("findAll")
    public List<UserInfo> findAll(){
        return userService.findall();
    }




}
