package com.study.mypizza.order.eventHandler;

import com.study.mypizza.order.repository.OrderRepository;
import com.study.mypizza.order.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventHandler {
    private final EventService eventService;
    private final OrderRepository orderRepository;


    @Bean
    // @Transactional은 실제 lambda 실행 시점에 적용되지 않으므로, 별도 서비스의 public 메서드로 DB 처리를 옮겨야 합니다.
//    @Transactional
    public Consumer<Message<String>> whenever_storeEvent_updateStatus() {
        return message -> {
            eventService.handleStoreEvent(message.getPayload());
        } ;
    }

    @Bean
    public Consumer<Message<String>> whenever_deliveryEvent_updateStatus() {
        return message -> {
            eventService.handleDeliveryEvent(message.getPayload());
        } ;
    }
}
