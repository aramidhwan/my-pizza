package com.study.mypizza.mypage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.study.mypizza.mypage.external") // 패키지 등록
public class MyPageApplication {
	public static ApplicationContext applicationContext ;
	public static void main(String[] args) {
		applicationContext = SpringApplication.run(MyPageApplication.class, args);
	}

}
