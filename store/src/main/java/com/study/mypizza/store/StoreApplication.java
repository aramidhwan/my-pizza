package com.study.mypizza.store;

import org.hibernate.resource.beans.container.spi.AbstractCdiBeanContainer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class StoreApplication {
	public static ApplicationContext applicationContext ;

	public static void main(String[] args) {
		applicationContext = SpringApplication.run(StoreApplication.class, args);
	}

}
