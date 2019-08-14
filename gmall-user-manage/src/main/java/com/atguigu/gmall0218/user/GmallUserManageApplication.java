package com.atguigu.gmall0218.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@MapperScan(basePackages = "com.atguigu.gmall0218.user.mapper")
@SpringBootApplication
@ComponentScan("com.atguigu.gmall0218")
public class GmallUserManageApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallUserManageApplication.class, args);
	}

}
