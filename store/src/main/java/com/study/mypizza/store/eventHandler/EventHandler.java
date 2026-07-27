package com.study.mypizza.store.eventHandler;

import com.study.mypizza.store.entity.StoreOrderDetail;
import com.study.mypizza.store.event.OrderDetailOrdered;
import com.study.mypizza.store.event.Ordered;
import com.study.mypizza.store.repository.StoreOrderDetailRepository;
import com.study.mypizza.store.repository.StoreOrderRepository;
import com.study.mypizza.store.repository.StoreRepository;
import com.study.mypizza.store.service.EventService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventHandler {
    private final EventService eventService;

    private final StoreRepository storeRepository ;
    private final StoreOrderRepository storeOrderRepository ;
    private final StoreOrderDetailRepository storeOrderDetailRepository ;

    @Bean
    // @Transactional은 실제 lambda 실행 시점에 적용되지 않으므로, 별도 서비스의 public 메서드로 DB 처리를 옮겨야 합니다.
//    @Transactional
    public Consumer<Message<Ordered>> whenever_ordered_orderAccept() {
        return message -> {
            Ordered ordered = message.getPayload() ;
            if (!ordered.validate()) return;

            log.info("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), ordered);
            eventService.handleEvent(ordered);
        } ;
    }

    @Bean
    @Transactional
    public Consumer<Message<OrderDetailOrdered>> whenever_orderDetailOrdered_saveStoreOrderDetail() {
        return eventMessage -> {
            OrderDetailOrdered orderDetailOrdered = eventMessage.getPayload() ;
            if (!orderDetailOrdered.validate()) return;

            log.info("xxxxxxxxxxxxxxxxx###xxxxxxxxxxxxxxxxxxxx [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), orderDetailOrdered);

//            Optional<StoreOrderDto> storeOrderDto = storeOrderRepository.findByOrderId(orderDetailOrdered.getOrderId())
//                    .map(StoreOrderDto::of);
//
//            // 주문 접수 된 건일 경우 detail 저장 --> 이렇게 할 경우 storeOrder save 지연으로 인해 storeOrderDetail 저장이 안되는 경우가 발생함
//            if ( storeOrderDto.isPresent() ) {
//            }
            StoreOrderDetail storeOrderDetail = StoreOrderDetail.builder()
                    .orderDetailId(orderDetailOrdered.getOrderDetailId())
//                    .orderId(orderDetailOrdered.getOrderId())
                    .itemId(orderDetailOrdered.getItemId())
                    .qty(orderDetailOrdered.getQty())
                    .pricePerOne(orderDetailOrdered.getPricePerOne())
                    .build();
            storeOrderDetailRepository.save(storeOrderDetail) ;
        } ;
    }
}
