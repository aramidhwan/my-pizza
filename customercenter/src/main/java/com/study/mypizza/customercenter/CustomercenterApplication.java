package com.study.mypizza.customercenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class CustomercenterApplication {
	public static ApplicationContext applicationContext ;

	public static void main(String[] args) {
		applicationContext = SpringApplication.run(CustomercenterApplication.class, args);
	}

}
