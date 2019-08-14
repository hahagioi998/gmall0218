package com.atguigu.gmall0218.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0218.bean.UserAddress;
import com.atguigu.gmall0218.bean.UserInfo;
import com.atguigu.gmall0218.config.RedisUtil;
import com.atguigu.gmall0218.service.UserService;
import com.atguigu.gmall0218.user.mapper.UserAddressMapper;
import com.atguigu.gmall0218.user.mapper.UserInfoMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;


import java.util.List;

/**
 * @author qiyu
 * @create 2019-07-23 12:53
 * @Description:
 */
@com.alibaba.dubbo.config.annotation.Service
public class UserServiceImpl implements UserService{
    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;

    @Autowired
    private  RedisUtil redisUtil;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;
    @Override
    public List<UserInfo> findall() {
        return userInfoMapper.selectAll();
    }



    /**
     * 根据userId查询用户地址
     * @param userId
     * @return
     */
    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return userAddressMapper.select(userAddress);
    }

    /**
     * 用户登录校验
     * @param userInfo
     * @return
     */
    @Override
    public UserInfo login(UserInfo userInfo) {
        String password = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        userInfo.setPasswd(password);

        UserInfo info = userInfoMapper.selectOne(userInfo);

        if(info != null){
            //将用户信息存储到redis中
            Jedis jedis = redisUtil.getJedis();
            jedis.setex(userKey_prefix+info.getId()+userinfoKey_suffix,
                        userKey_timeOut, JSON.toJSONString(info));
            jedis.close();
            return info;
        }

        return null;
    }


    /**
     * 校验redis中是否有用户信息
     * @param userId
     * @return
     */
    @Override
    public UserInfo verify(String userId) {
        // 去缓存中查询是否有redis
        Jedis jedis = redisUtil.getJedis();
        String key = userKey_prefix+userId+userinfoKey_suffix;

        String userJson = jedis.get(key);

        //延长时效
        jedis.expire(key,userKey_timeOut);

        if(userJson !=null){
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return userInfo;
        }

        return null;

    }


}
