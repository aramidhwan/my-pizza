package com.study.mypizza.delivery.service;

import com.study.mypizza.delivery.entity.Delivery;
import com.study.mypizza.delivery.enums.OrderStatus;
import com.study.mypizza.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.json.JSONObject;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventService {
    private final DeliveryRepository deliveryRepository ;

    @Transactional
    public void handleStoreEvent(String payload) {
        JSONObject json = new JSONObject(payload);
        String eventType = json.getString("eventType") ;  // "ORDER_ACCEPTED"
        String status = json.getString("status");

        if (!"StatusUpdated".equals(eventType)) return;
        if (!OrderStatus.COOKED.getStatus().equals(status)) return;

        log.info("■■■ kafka event received..!! : {}", payload);

        Delivery delivery = Delivery.builder()
                .orderId(json.getLong("orderId"))
                .storeId(json.getLong("storeId"))
                .status(OrderStatus.DELIVERY_ACCEPTED)
                .ownerNo(1)
                .build();
        deliveryRepository.save(delivery);
    }
}
