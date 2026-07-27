package com.study.mypizza.store.service;

import com.study.mypizza.store.entity.Store;
import com.study.mypizza.store.entity.StoreOrder;
import com.study.mypizza.store.enums.OrderStatus;
import com.study.mypizza.store.event.Ordered;
import com.study.mypizza.store.exception.MyPizzaException;
import com.study.mypizza.store.repository.StoreOrderRepository;
import com.study.mypizza.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventService {
//    private final ModelMapper modelMapper;
    private final StoreRepository storeRepository;
    private final StoreOrderRepository storeOrderRepository;

    @Transactional
    public void handleEvent(Ordered ordered) {
        List<Store> storeList = storeRepository.findByRegionNmAndOpenYNTrue(ordered.getRegionNm()) ;
        int openStoreCnt = storeList.size();
        // 주문이 들어온 regionNm에 Open된 Sotre가 한군데라도 있으면 주문접수
        if (openStoreCnt > 0) {
            int random = new Random().nextInt(openStoreCnt) ;

            StoreOrder storeOrder = StoreOrder.builder()
                    .store(Store.builder().storeId(storeList.get(random).getStoreId()).build())
                    .orderId(ordered.getOrderId())
                    .status(OrderStatus.ORDER_ACCEPTED)
                    .build();
            storeOrder.assignStoreOrderDetail(ordered.getOrderDetailDtoList());
            storeOrderRepository.save(storeOrder) ;
        } else {
            throw new MyPizzaException("지역구 ["+ordered.getRegionNm()+"]에 영업중인 상점이 없습니다!") ;
        }
    }
}
