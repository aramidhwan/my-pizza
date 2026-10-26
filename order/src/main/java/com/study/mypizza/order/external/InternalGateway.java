package com.study.mypizza.order.external;

import com.study.mypizza.order.config.FeignClientConfiguration;
import com.study.mypizza.order.dto.GatewayDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

// Spring Cloud OpenFeign 공식문서
// https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/#netflix-feign-starter

// FeignClient 이름(name)을 Eureka 서버에 등록된 상대 클라이언트 이름(spring.application.name)으로 지정하면
// 해당 이름을 가진 Eureka 클라이언트에게 API 요청을 수행한다.
// 이렇게 Eureka 클라이언트 이름을 사용하면 "별도의 URL 정보가 필요 없다".
//@FeignClient(name="STORE", fallbackFactory = StoreServiceFallbackFactory.class)
@FeignClient(name="GATEWAY", configuration = FeignClientConfiguration.class)
public interface InternalGateway {
    Logger log = LoggerFactory.getLogger(InternalGateway.class);

    @GetMapping("/store-service/stores/checkOpenYn")
    @Retry(name = "RETRY-checkOpenYn", fallbackMethod = "retryFallbackCheckOpenYn")
    @CircuitBreaker(name = "CIRCUIT-checkOpenYn", fallbackMethod = "circuitBreakerFallback")
    GatewayDto<String> checkOpenYn(@RequestParam("regionNm") String regionNm) ;

    default GatewayDto<String> retryFallbackCheckOpenYn(String regionNm, Throwable cause) {
        if (cause instanceof feign.codec.DecodeException) {
            log.error("■■■ [ERROR] 상대서버(STORE) 다운으로 HTML 페이지가 수신되어 디코딩에 실패했습니다.");
            return GatewayDto.<String>builder()
                    .dto("[ERROR] 상대서버(STORE) 다운!")
                    .build();
        } else {
            log.error("[InternalGateway] retryFallbackCheckOpenYn. regionNm={}", regionNm, cause);
        }
        return GatewayDto.<String>builder()
                .dto("[ERROR] API 호출 실패!")
                .build();
    }

    // io.github.resilience4j.circuitbreaker.CallNotPermittedException
    default GatewayDto<String> circuitBreakerFallback(String regionNm, Throwable cause) {
        log.warn("[InternalGateway] circuitBreakerFallback. regionNm={}", regionNm, cause);
        return GatewayDto.<String>builder()
                .dto("[ERROR] 상대서버(STORE) 다운! (Circuit Open)")
                .build();
    }

    @GetMapping("/store-service/stores/checkOrderCancel/{orderId}")
    boolean checkOrderCancel(@PathVariable("orderId") Long orderId) ;

    @GetMapping("/store-service/stores/getOpenRegionNmList")
    @Retry(name = "RETRY-getOpenRegionNmList", fallbackMethod = "retryFallbackGetOpenRegionNmList")
    @CircuitBreaker(name = "CIRCUIT-getOpenRegionNmList", fallbackMethod = "circuitBreakerFallbackGetOpenRegionNmList")
    GatewayDto<List<String>> getOpenRegionNmList();

    default GatewayDto<List<String>> retryFallbackGetOpenRegionNmList(Throwable cause) {
        if (cause instanceof feign.codec.DecodeException) {
            log.error("■■■ [ERROR] 상대서버(STORE) 다운으로 HTML 페이지가 수신되어 디코딩에 실패했습니다.");
        } else {
            log.error("[InternalGateway] retryFallbackGetOpenRegionNmList API 호출 실패", cause);
        }
        return GatewayDto.<List<String>>builder()
                .dto(List.of("[ERROR] 일시장애 - retry"))
                .build();
    }

    // io.github.resilience4j.circuitbreaker.CallNotPermittedException
    default GatewayDto<List<String>> circuitBreakerFallbackGetOpenRegionNmList(Throwable cause) {
        log.warn("[InternalGateway] circuitBreakerFallbackGetOpenRegionNmList.", cause);
        return GatewayDto.<List<String>>builder()
                .dto(List.of("[ERROR] 일시장애 - circuit open!"))
                .build();
    }
}

