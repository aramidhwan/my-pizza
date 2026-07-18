package com.study.mypizza.delivery.external;

import com.study.mypizza.delivery.config.FeignClientConfiguration;
import com.study.mypizza.delivery.dto.GatewayDto;
import com.study.mypizza.delivery.dto.StoreDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Spring Cloud OpenFeign 공식문서
// https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/#netflix-feign-starter

// FeignClient 이름(name)을 Eureka 서버에 등록된 상대 클라이언트 이름(spring.application.name)으로 지정하면
// 해당 이름을 가진 Eureka 클라이언트에게 API 요청을 수행한다.
// 이렇게 Eureka 클라이언트 이름을 사용하면 "별도의 URL 정보가 필요 없다".
//@FeignClient(name="STORE", fallbackFactory = StoreServiceFallbackFactory.class)
@FeignClient(name="GATEWAY", configuration = FeignClientConfiguration.class)
public interface InternalGateway {
    Logger log = LoggerFactory.getLogger(InternalGateway.class);

    @GetMapping("/store-service/stores/getStoreNm")
    @Retry(name = "RETRY-getStoreNm", fallbackMethod = "retryGetStoreNm")
    @CircuitBreaker(name = "CIRCUIT-getStoreNm", fallbackMethod = "circuitFallbackGetStoreNm")
        // [주의] @Cacheable 메소드는 같은 클래스 내부에서 호출하면 동작하지 않는다.
    GatewayDto<StoreDto> getStoreNm(@RequestParam("storeId") Long storeId) ;

    default GatewayDto<StoreDto> retryGetStoreNm(Long storeId, Throwable cause) {
        log.warn("[InternalGateway] retryGetStoreNm. storeId={}, cause={}", storeId, cause.getMessage());
        return GatewayDto.<StoreDto>builder()
                .bizSuccess(1)
                .dto(StoreDto.builder().storeNm("[상점명]일시장애(Retry)").build())
                .build() ;
    }

    // io.github.resilience4j.circuitbreaker.CallNotPermittedException
    default GatewayDto<StoreDto> circuitFallbackGetStoreNm(Long storeId, Throwable cause) {
        log.warn("[InternalGateway] circuitFallbackGetStoreNm. storeId={}, cause={}", storeId, cause.getMessage());
        return GatewayDto.<StoreDto>builder()
                .bizSuccess(1)
                .dto(StoreDto.builder().storeNm("[상점명]일시장애(Circuit)").build())
                .build() ;
    }
}
