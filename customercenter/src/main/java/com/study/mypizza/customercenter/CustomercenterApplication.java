package com.study.mypizza.customercenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.study.mypizza.customercenter.external") // 패키지 등록
public class CustomercenterApplication {
	public static ApplicationContext applicationContext ;

	public static void main(String[] args) {
		applicationContext = SpringApplication.run(CustomercenterApplication.class, args);
	}

}
