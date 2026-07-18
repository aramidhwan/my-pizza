package com.study.mypizza.delivery.service;

import com.study.mypizza.delivery.dto.*;
import com.study.mypizza.delivery.external.InternalGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {
    private final InternalGateway internalGateway;

    // [주의] @Cacheable 메소드는 같은 클래스 내부에서 호출하면 동작하지 않는다.
    @Cacheable(value = "storeNm", key="#storeId", unless = "#result == null || #result.startsWith('[ERROR]')")
    public String getStoreNm(Long storeId) {
        log.debug("### storeNm 캐시 호출 !! : storeId = {}", storeId);
        GatewayDto<StoreDto> gatewayDto = internalGateway.getStoreNm(storeId) ;
        if (gatewayDto.getBizSuccess() != 0) {
            if ( gatewayDto.getDto() != null ) {
                gatewayDto.getDto().setStoreNm("[ERROR]"+gatewayDto.getDto().getStoreNm());
            } else {
                gatewayDto.setDto(StoreDto.builder().storeNm("[ERROR]").build());
            }
        }
        return gatewayDto.getDto().getStoreNm() ;
    }
}
