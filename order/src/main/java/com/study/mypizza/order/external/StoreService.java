package com.study.mypizza.order.external;

import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

// Spring Cloud OpenFeign 공식문서
// https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/#netflix-feign-starter

@FeignClient(name="storeService", fallbackFactory = StoreServiceFallbackFactory.class)
public interface StoreService {
    @GetMapping("/stores/checkOpenYn")
    public boolean checkOpenYn(@RequestParam("regionNm") String regionNm) ;

    @GetMapping("/stores/checkOrderCancel/{orderId}")
    public boolean checkOrderCancel(@PathVariable("orderId") Long orderId) ;
}

