package com.study.mypizza.delivery.eventHandler;

import com.study.mypizza.delivery.entity.Delivery;
import com.study.mypizza.delivery.enums.OrderStatus;
import com.study.mypizza.delivery.event.StatusUpdated;
import com.study.mypizza.delivery.repository.DeliveryRepository;
import com.study.mypizza.delivery.service.EventService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventHandler {
    private final EventService eventService;

    @Bean
    public Consumer<Message<String>> whenever_Cooked_DeliveryAccept() {
        return message -> {
            eventService.handleStoreEvent(message.getPayload());
        } ;
    }
}
