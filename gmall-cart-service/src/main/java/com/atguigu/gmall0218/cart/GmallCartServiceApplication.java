package com.atguigu.gmall0218.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;
@EnableTransactionManagement
@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.gmall0218.cart.mapper")
@ComponentScan("com.atguigu.gmall0218")
public class GmallCartServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallCartServiceApplication.class, args);
	}

}
