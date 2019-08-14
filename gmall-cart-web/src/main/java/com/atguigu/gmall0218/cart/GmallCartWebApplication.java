package com.atguigu.gmall0218.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.atguigu.gmall0218")
public class GmallCartWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallCartWebApplication.class, args);
	}

}
