package com.atguigu.gmall0218.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.atguigu.gmall0218")
public class GmallOrderWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallOrderWebApplication.class, args);
	}

}
