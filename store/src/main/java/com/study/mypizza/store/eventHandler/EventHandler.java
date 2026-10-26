package com.study.mypizza.store.eventHandler;

import com.study.mypizza.store.enums.OrderStatus;
import com.study.mypizza.store.repository.StoreOrderDetailRepository;
import com.study.mypizza.store.repository.StoreOrderRepository;
import com.study.mypizza.store.repository.StoreRepository;
import com.study.mypizza.store.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
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
    public Consumer<Message<String>> whenever_ordered_orderAccept() {
        return message -> {
            JSONObject json = new JSONObject(message.getPayload());
            String status = json.optString("status") ;  // "OrderAccepted"

            if (OrderStatus.ORDERED == OrderStatus.valueOf(status)) {
                eventService.handleOrderedEvent(message.getPayload());
            } else if (OrderStatus.ORDER_CANCELLED == OrderStatus.valueOf(status)) {
                eventService.handleOrderCancelledEvent(message.getPayload());
            }
        } ;
    }
}
