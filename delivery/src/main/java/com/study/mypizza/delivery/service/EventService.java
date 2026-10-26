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
    public void handleEvent(String payload) {
        JSONObject json = new JSONObject(payload);
        String status = json.optString("status");
        Long orderId = json.optLong("orderId");

        // Delivery MSA 는 COOKED 이벤트만 수신한다.
        if ( OrderStatus.COOKED == OrderStatus.valueOf(status) ) {
            log.info("👉 카프카 이벤트 수신..!! : {}, orderId=[{}], {}", String.format("%-20s", status), orderId, payload);

            // 신규 배달건 생성
            boolean isSucceeded = createDelivery(payload, orderId, json);
            if (!isSucceeded) return;

            // kafka 순서 역전된 pendingEvent가 있으면 처리
            // delivery msa 는 COOKED 이벤트 한가지만 consume 하므로 pending 시킬 event 자체가 없다!!
//            processPendingEvent(json);
        }
    }

    private boolean createDelivery(String payload, Long orderId, JSONObject json) {
        // kafka 중복 이벤트 방지
        if (deliveryRepository.existsByOrderId(orderId)) {
            log.error("👉 배달 테이블(t_delivery)에 이미 등록된 주문 입니다. orderId=[{}], eventType={}, payload={}", orderId, json.optString("eventType"), payload);
            return false;
        }

        Delivery delivery = Delivery.builder()
                .orderId(orderId)
                .storeId(json.optLong("storeId"))
                .status(OrderStatus.DELIVERY_ACCEPTED)
                .ownerNo(1)
                .build();
        deliveryRepository.save(delivery);
        return true;
    }
}
