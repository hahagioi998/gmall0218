package com.atguigu.gmall0218.handler;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author qiyu
 * @create 2019-08-02 20:49
 * @Description:拦截器配置
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
