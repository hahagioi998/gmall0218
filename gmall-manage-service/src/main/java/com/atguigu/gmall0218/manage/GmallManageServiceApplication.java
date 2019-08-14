package com.atguigu.gmall0218.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;
@EnableTransactionManagement
@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.gmall0218.manage.mapper")
@ComponentScan("com.atguigu.gmall0218")
public class GmallManageServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallManageServiceApplication.class, args);
	}

}
