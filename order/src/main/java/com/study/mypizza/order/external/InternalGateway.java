package com.study.mypizza.order.external;

import com.study.mypizza.order.config.FeignClientConfiguration;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

// Spring Cloud OpenFeign 공식문서
// https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/#netflix-feign-starter

// FeignClient 이름(name)을 Eureka 서버에 등록된 상대 클라이언트 이름(spring.application.name)으로 지정하면
// 해당 이름을 가진 Eureka 클라이언트에게 API 요청을 수행한다.
// 이렇게 Eureka 클라이언트 이름을 사용하면 "별도의 URL 정보가 필요 없다".
//@FeignClient(name="STORE", fallbackFactory = StoreServiceFallbackFactory.class)
@FeignClient(name="GATEWAY", configuration = FeignClientConfiguration.class)
public interface InternalGateway {
    @GetMapping("/store-service/stores/checkOpenYn")
    @Retry(name = "RETRY-checkOpenYn", fallbackMethod = "retryFallback")
    @CircuitBreaker(name = "CIRCUIT-checkOpenYn", fallbackMethod = "circuitBreakerFallback")
    String checkOpenYn(@RequestParam("regionNm") String regionNm) ;

    @GetMapping("/store-service/stores/checkOrderCancel/{orderId}")
    boolean checkOrderCancel(@PathVariable("orderId") Long orderId) ;

    default String retryFallback(Exception cause) {
        System.out.println("[InternalGateway] retryFallback : " + cause.getMessage());
        return cause.getMessage();
    }

    // io.github.resilience4j.circuitbreaker.CallNotPermittedException
    default String circuitBreakerFallback(Exception cause) {
        System.out.println("[InternalGateway] " + cause.getMessage());
        return cause.getMessage();
    }
}

