package com.study.mypizza.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class DeliveryApplication {
	public static ApplicationContext applicationContext ;

	public static void main(String[] args) {
		applicationContext = SpringApplication.run(DeliveryApplication.class, args);
	}

}
