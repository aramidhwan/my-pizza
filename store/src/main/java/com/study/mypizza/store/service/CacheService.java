package com.study.mypizza.store.service;

import com.study.mypizza.store.dto.*;
import com.study.mypizza.store.external.InternalGateway;
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
    @Cacheable(value = "itemNm", key="#itemId", unless = "#result == null || #result.startsWith('[ERROR]')")
    public String getItemNm(Long itemId) {
        log.debug("### itemNm 캐시 호출 !! : itemId = {}", itemId);
        GatewayDto<ItemDto> gatewayDto = internalGateway.getItemNm(itemId) ;
        if (gatewayDto.getBizSuccess() != 0) {
            if ( gatewayDto.getDto() != null ) {
                gatewayDto.getDto().setItemNm("[ERROR]"+gatewayDto.getDto().getItemNm());
            } else {
                gatewayDto.setDto(ItemDto.builder().itemNm("[ERROR]").build());
            }
        }
        return gatewayDto.getDto().getItemNm() ;
    }
}
