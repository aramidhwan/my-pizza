package com.study.mypizza.order.service;

import com.study.mypizza.order.entity.Order;
import com.study.mypizza.order.enums.OrderStatus;
import com.study.mypizza.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventService {
    private final OrderRepository orderRepository;

    @Transactional
    public void handleStoreEvent(String payload) {
        JSONObject json = new JSONObject(payload);
        String eventType = json.getString("eventType") ;  // "ORDER_ACCEPTED"
        Long orderId = json.getLong("orderId");

        log.info("■■■ kafka EVENT received..!! : {}", payload);

        try {
            // 기존 주문(Order) 검색
            Optional<Order> orderOptional = orderRepository.findById(orderId) ;
            if ( orderOptional.isPresent()) {
                Order order = orderOptional.get();

                // 중복처리 방지
                if (OrderStatus.ORDER_ACCEPTED.toString().equals(eventType) && !OrderStatus.ORDERED.equals(order.getStatus()) ) {
                    log.warn("■■■ [{}] event 상태이상!!, Skip..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), payload);
                    return ;
                } else if (OrderStatus.COOKED.toString().equals(eventType) && !OrderStatus.ORDER_ACCEPTED.equals(order.getStatus()) ) {
                    log.warn("■■■ [{}] event 상태이상!!, Skip..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), payload);
                    return ;
                }

                // OrderAccepted, Cooked 상태 업데이트
                order.statusUpdate(OrderStatus.valueOf(eventType));

                // OrderAccepted 인 경우 storeId 셋팅
                if (OrderStatus.ORDER_ACCEPTED.toString().equals(eventType)) {
                    order.assignStore(json.getLong("storeId"));
                }

                orderRepository.save(order);
            }
        } catch (NumberFormatException | JSONException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void handleDeliveryEvent(String payload) {
        JSONObject json = new JSONObject(payload);
        Long orderId = json.getLong("orderId");
        String eventType = json.getString("eventType");   // ORDER_ACCEPTED

        log.info("■■■ kafka event received..!! : {}", payload);

        Optional<Order> orderOptional = orderRepository.findById(orderId) ;
        if ( orderOptional.isPresent()) {
            Order order = orderOptional.get();

            // 중복처리 방지
            if (OrderStatus.DELIVERY_ACCEPTED.getStatus().equals(eventType) && OrderStatus.COOKED != order.getStatus()) {
                log.warn("■■■ [{}] event 상태이상!!, Skip..!! : orderId={}, status={}, currentStatus={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, eventType, order.getStatus());
                return ;
            } else if (OrderStatus.DELIVERY_STARTED.getStatus().equals(eventType) && OrderStatus.DELIVERY_ACCEPTED != order.getStatus()) {
                log.warn("■■■ [{}] event 상태이상!!, Skip..!! : orderId={}, status={}, currentStatus={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, eventType, order.getStatus());
                return ;
            } else if (OrderStatus.DELIVERED.getStatus().equals(eventType) && OrderStatus.DELIVERY_STARTED != order.getStatus()) {
                log.warn("■■■ [{}] event 상태이상!!, Skip..!! : orderId={}, status={}, currentStatus={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, eventType, order.getStatus());
                return ;
            }

            order.statusUpdate(OrderStatus.valueOf(eventType));
            orderRepository.save(order);
        }
    }
}
