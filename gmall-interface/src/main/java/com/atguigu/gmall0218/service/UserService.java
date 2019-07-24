package com.atguigu.gmall0218.service;

import com.atguigu.gmall0218.bean.UserAddress;
import com.atguigu.gmall0218.bean.UserInfo;

import java.util.List;

public interface UserService {
    /**
     * 查询所有的数据
     * @return
     */
    List<UserInfo> findall();

    /**
     * 根据userId查询地址
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddressList(String userId);

}
