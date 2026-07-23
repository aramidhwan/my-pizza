package com.study.mypizza.mypage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.study.mypizza.mypage.external") // 패키지 등록
@EnableJpaAuditing
public class MyPageApplication {
	public static ApplicationContext applicationContext ;
	public static void main(String[] args) {
		applicationContext = SpringApplication.run(MyPageApplication.class, args);
	}

}
