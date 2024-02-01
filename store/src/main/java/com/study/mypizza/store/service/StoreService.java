package com.study.mypizza.store.service;

import com.study.mypizza.store.entity.Store;
import com.study.mypizza.store.entity.StoreOrder;
import com.study.mypizza.store.repository.StoreOrderRepository;
import com.study.mypizza.store.repository.StoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
public class StoreService {
    @Autowired
    private StoreRepository storeRepository ;
    @Autowired
    private StoreOrderRepository storeOrderRepository ;

    public boolean checkOpenYn(String regionNm) throws Exception {
        log.debug("##### /store/chkOpenYn called in [{}]", regionNm) ;
        boolean openYn = false ;

        // CircuitBreaker timeout 검증을 위한 임시 코드
        if ("서초구".equals(regionNm.trim())) {
            log.debug("##### 서초구 Sleeping 3s...") ;
            Thread.sleep(3000);
        }
        // CircuitBreaker timeout 검증을 위한 임시 코드
        if ("종로구".equals(regionNm.trim())) {
            log.debug("##### 종로구 Sleeping 5s...") ;
            Thread.sleep(5000);
        }

        List<Store> storeList = storeRepository.findByRegionNmAndOpenYN(regionNm, true);
        int openStoreCnt = storeList.size();

        // 주문이 들어온 regionNm에 Open된 Sotre가 한군데라도 있으면 true를 리턴
        if (openStoreCnt > 0) {
            openYn = true ;
        } else {
            log.debug("##### No Store opened in [{}]", regionNm) ;
        }

        return openYn ;
    }

    public boolean checkOrderCancel(@PathVariable("orderId") Long orderId) {
        boolean cancelled = false ;
        Optional<StoreOrder> optional = storeOrderRepository.findByOrderId(orderId) ;

        // StoreOrder에 해당 orderId가 없을 경우 "주문취소" 가능
        if ( !optional.isPresent() ) {
            cancelled = true ;
        }

        return cancelled ;
    }
}
