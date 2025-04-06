package com.study.mypizza.store.eventHandler;

import com.study.mypizza.store.entity.Store;
import com.study.mypizza.store.entity.StoreOrder;
import com.study.mypizza.store.entity.StoreOrderDetail;
import com.study.mypizza.store.enums.OrderStatus;
import com.study.mypizza.store.event.OrderDetailOrdered;
import com.study.mypizza.store.event.Ordered;
import com.study.mypizza.store.repository.StoreOrderDetailRepository;
import com.study.mypizza.store.repository.StoreOrderRepository;
import com.study.mypizza.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventHandler {
    private final StoreRepository storeRepository ;
    private final StoreOrderRepository storeOrderRepository ;
    private final StoreOrderDetailRepository storeOrderDetailRepository ;

    @Bean
    @Transactional
    public Consumer<Message<Ordered>> whenever_ordered_orderAccept() {
        return eventMessage -> {
            Ordered ordered = eventMessage.getPayload() ;
            if (!ordered.validate()) return;

            log.trace("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), ordered.toString());

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
                storeOrderRepository.save(storeOrder) ;
            }
        } ;
    }

    @Bean
    @Transactional
    public Consumer<Message<OrderDetailOrdered>> whenever_orderDetailOrdered_saveStoreOrderDetail() {
        return eventMessage -> {
            OrderDetailOrdered orderDetailOrdered = eventMessage.getPayload() ;
            if (!orderDetailOrdered.validate()) return;

            log.trace("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), orderDetailOrdered.toString());

//            Optional<StoreOrderDto> storeOrderDto = storeOrderRepository.findByOrderId(orderDetailOrdered.getOrderId())
//                    .map(StoreOrderDto::of);
//
//            // 주문 접수 된 건일 경우 detail 저장 --> 이렇게 할 경우 storeOrder save 지연으로 인해 storeOrderDetail 저장이 안되는 경우가 발생함
//            if ( storeOrderDto.isPresent() ) {
//            }
            StoreOrderDetail storeOrderDetail = StoreOrderDetail.builder()
                    .orderDetailId(orderDetailOrdered.getOrderDetailId())
                    .orderId(orderDetailOrdered.getOrderId())
                    .itemId(orderDetailOrdered.getItemId())
                    .qty(orderDetailOrdered.getQty())
                    .pricePerOne(orderDetailOrdered.getPricePerOne())
                    .build();
            storeOrderDetailRepository.save(storeOrderDetail) ;
        } ;
    }
}
