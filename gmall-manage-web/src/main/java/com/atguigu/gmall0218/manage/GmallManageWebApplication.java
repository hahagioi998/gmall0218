package com.atguigu.gmall0218.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.atguigu.gmall0218")
public class GmallManageWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallManageWebApplication.class, args);
	}

}
