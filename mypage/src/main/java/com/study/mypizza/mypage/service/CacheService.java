package com.study.mypizza.mypage.service;

import com.study.mypizza.mypage.dto.*;
import com.study.mypizza.mypage.external.InternalGateway;
import com.study.mypizza.mypage.mapper.MyPageMapper;
import com.study.mypizza.mypage.repository.MyPageOrderDetailRepository;
import com.study.mypizza.mypage.repository.MyPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
