package com.atguigu.gmall0218.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;


import org.springframework.context.annotation.Configuration;

/**
 * @author qiyu
 * @create 2019-07-28 15:14
 * @Description:
 */
@Configuration
public class RedisConfig {
    //读取配置文件中的redis的ip地址
    @Value("${spring.redis.host:disabled}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    @Bean
    public RedisUtil getRedisUtil(){
        if(host.equals("disabled")){
            return null;
        }
        RedisUtil redisUtil=new RedisUtil();

        redisUtil.initJedisPool(host,port,database);

        return redisUtil;
    }
}
