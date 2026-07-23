package com.study.mypizza.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.study.mypizza.order.external") // 패키지 등록
@EnableJpaAuditing
public class OrderApplication {

	public static ApplicationContext applicationContext;

	public static void main(String[] args) {
		applicationContext = SpringApplication.run(OrderApplication.class, args);
	}
}
