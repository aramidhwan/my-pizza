package com.study.mypizza.store.eventHandler.workers;

import com.study.mypizza.store.entity.Store;
import com.study.mypizza.store.entity.StoreOrder;
import com.study.mypizza.store.event.Ordered;
import com.study.mypizza.store.repository.StoreOrderRepository;
import com.study.mypizza.store.repository.StoreRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@Setter
@Slf4j
public class OrderAcceptWorkerThread implements Runnable {

    @Autowired
    StoreRepository storeRepository ;
    @Autowired
    StoreOrderRepository storeOrderRepository ;
    private Ordered ordered;

    @Override
    public void run() {
        orderAccept();
    }

    private void orderAccept() {
        log.debug("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), ordered.toString());

        List<Store> storeList = storeRepository.findByRegionNmAndOpenYN(ordered.getRegionNm(), Boolean.TRUE) ;
        int openStoreCnt = storeList.size();

        // 주문이 들어온 regionNm에 Open된 Sotre가 한군데라도 있으면
        // 주문접수
        if (openStoreCnt > 0) {
            int random = new Random().nextInt(openStoreCnt) ;

            StoreOrder storeOrder = new StoreOrder() ;
            BeanUtils.copyProperties(ordered, storeOrder);
            storeOrder.setStoreId(storeList.get(random).getStoreId());
            storeOrder.setStatus("OrderAccepted");
            storeOrderRepository.save(storeOrder) ;
        }
    }
}