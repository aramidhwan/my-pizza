package com.study.mypizza.store.controller;

import com.study.mypizza.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GatewayController {
    private final StoreService storeService;

    // Order MSA에서 호출되는 메소드 (지역의 상점 오픈 여부 체크)
    @GetMapping("/stores/checkOpenYn")
    public String checkOpenYn(@RequestParam("regionNm") String regionNm) {
        return storeService.checkOpenYn(regionNm);
    }

    // MyPage MSA에서 호출되는 메소드 (상점 명칭 가져오기)
    @GetMapping("/stores/getStoreNm")
    public String getStoreNm(@RequestParam("storeId") Long storeId) {
        return storeService.getStoreNm(storeId);
    }

    // Order MSA에서 호출되는 메소드 (주문취소 가능여부 체크)
    @GetMapping("/stores/checkOrderCancel/{orderId}")
    public boolean checkOrderCancel(@PathVariable("orderId") Long orderId) {
        return storeService.checkOrderCancel(orderId) ;
    }
}
