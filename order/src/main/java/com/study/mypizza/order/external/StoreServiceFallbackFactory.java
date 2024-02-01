package com.study.mypizza.order.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;

@Component
@Slf4j
public class StoreServiceFallbackFactory implements FallbackFactory<StoreService> {
    @Override
    public StoreService create(Throwable cause) {
        return new StoreService() {
            @Override
            public boolean checkOpenYn(String regionNm) {
                printWarn();

                return false ;
            }

            @Override
            public boolean checkOrderCancel(Long orderId) {
                printWarn();

                return false;
            }

            private void printWarn() {
                if ( cause instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
                    log.warn("####### Fallback (Circuit-OPEN)CallNotPermittedException occured in [{}] caused by [{}]", this.getClass().getName(), cause.toString());

                    // TimeLimiter 설정시간 이내라면 Retryable 가능
                    // (feign read-timeout 등으로 인해 TimeLimiter 설정시간 이내에 fallback 된 경우)
                } else if ( cause instanceof java.util.concurrent.ExecutionException) {
                    log.warn("####### Fallback ExecutionException occured in [{}] caused by [{}]", this.getClass().getName(), cause.toString());

                    // 그 외
                } else {
                    log.warn("####### Fallback in [{}] caused by [{}]", this.getClass().getName(), cause.toString());
                }
            }
        };
    }
}
