package com.study.mypizza.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients(basePackages = "com.study.mypizza.store.external") // 패키지 등록
public class StoreApplication {
	public static ApplicationContext applicationContext ;

	public static void main(String[] args) {
		applicationContext = SpringApplication.run(StoreApplication.class, args);
	}

}
