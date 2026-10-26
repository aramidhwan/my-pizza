package com.study.mypizza.order.service;

import com.study.mypizza.order.dto.GatewayDto;
import com.study.mypizza.order.external.InternalGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {
    private final InternalGateway internalGateway;

    // [주의] @Cacheable 메소드는 같은 클래스 내부에서 호출하면 동작하지 않는다.
    // 파라미터가 없으므로 내부적으로 SimpleKey.EMPTY 가 키가 됨
    @Cacheable(value = "openRegionNmList", unless = "#result == null || #result.toString().contains('[ERROR]')")
    public List<String> getOpenRegionNmList() {
        log.warn("### 주문지역 리스트(getOpenRegionNmList) 캐시 Miss !!");
        GatewayDto<List<String>> gatewayDto = internalGateway.getOpenRegionNmList() ;
        if (gatewayDto.getBizSuccess() != 0) {
            return List.of("[ERROR] 일시장애") ;
        }

        return gatewayDto.getDto();
    }

    // [주의] @Cacheable 메소드는 같은 클래스 내부에서 호출하면 동작하지 않는다.
    @Cacheable(value = "openYN", key="#regionNm", unless = "#result == null || #result.startsWith('[ERROR]')")
    public String checkOpenYn(String regionNm) {
        log.warn("### 주문지역 상점 오픈여부 체크(checkOpenYn) 캐시 Miss !!");
        GatewayDto<String> gatewayDto = internalGateway.checkOpenYn(regionNm) ;
        if (gatewayDto.getBizSuccess() != 0) {
            return "[ERROR] 일시장애" ;
        }

        return gatewayDto.getDto();
    }

    /**
     * openRegionNmList 캐시 초기화 (Evict)
     */
    @CacheEvict(value = "openRegionNmList", allEntries = true)
    public void clearOpenRegionNmListCache() {
        log.warn("### 주문지역 리스트(openRegionNmList) 캐시 Clear !!");
    }

    /**
     * openYN 캐시 전체 초기화 (Evict) - regionNm에 상관없이 allEntries 삭제!
     */
    @CacheEvict(value = "openYN", allEntries = true)
    public void clearAllOpenYnCache() {
        log.warn("### 주문지역 상점 오픈여부 체크(openYN) 캐시 전체 Clear !!");
    }
}
