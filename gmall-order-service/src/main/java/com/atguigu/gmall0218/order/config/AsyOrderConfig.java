package com.atguigu.gmall0218.order.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author qiyu
 * @create 2019-08-11 1:31
 * @Description:利用多线程实现异步并发操作
 */
@Configuration
@EnableAsync
public class AsyOrderConfig implements AsyncConfigurer {
    @Override
    @Bean
    public Executor getAsyncExecutor() {
        // 获取线程池 – 数据库的连接池
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        // 设置线程数
        threadPoolTaskExecutor.setCorePoolSize(10);
        // 设置最大连接数
        threadPoolTaskExecutor.setMaxPoolSize(100);
        // 设置等待队列，如果10个不够，可以有100个线程等待 缓冲池
        threadPoolTaskExecutor.setQueueCapacity(100);
        // 初始化操作
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }
}
