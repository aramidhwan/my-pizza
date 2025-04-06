package com.study.mypizza.order.eventHandler;

import com.study.mypizza.order.entity.Order;
import com.study.mypizza.order.repository.OrderRepository;
import com.study.mypizza.order.enums.OrderStatus;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Consumer;

@Service
@Slf4j
public class EventHandler {
    @Autowired
    OrderRepository orderRepository;

    @Bean
    @Transactional
    public Consumer<Message<String>> whenever_storeEvent_updateStatus() {
        return message -> {
            String jsonString = message.getPayload() ;

            JSONObject json = new JSONObject(jsonString);
            log.trace("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), jsonString);
            Long orderId = json.getLong("orderId");
            String status = json.getString("status");   // "ORDER_ACCEPTED"
            setBusiness("상태업데이트");  // Jennifer 추적용

            Optional<Order> orderOptional = orderRepository.findById(orderId) ;
            if ( orderOptional.isPresent()) {
                Order order = orderOptional.get();

                // 중복처리 방지
                if (OrderStatus.ORDER_ACCEPTED.toString().equals(status) && OrderStatus.ORDERED != order.getStatus() ) {
                    log.warn("### [{}] event 상태이상!!, Skip..!! : orderId={}, status={}, currentStatus={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, status, order.getStatus());
                    return ;
                } else if (OrderStatus.COOKED.toString().equals(status) && OrderStatus.ORDER_ACCEPTED != order.getStatus() ) {
                    log.warn("### [{}] event 상태이상!!, Skip..!! : orderId={}, status={}, currentStatus={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, status, order.getStatus());
                    return ;
                }

                // OrderAccepted, Cooked 상태 업데이트
                order.statusUpdate(OrderStatus.valueOf(status));

                // OrderAccepted 인 경우 storeId 셋팅
                if (OrderStatus.ORDER_ACCEPTED.toString().equals(status)) {
                    try {
                        order.assignStore(json.getLong("storeId"));
                    } catch (NumberFormatException | JSONException ex) {
                        // do noting
                    }
                }
                orderRepository.save(order);
            }
        } ;
    }

    // only for Jennifer APM
    private void setBusiness(String 신규주문) {
    }

    @Bean
    @Transactional
    public Consumer<Message<String>> whenever_deliveryEvent_updateStatus() {
        return message -> {
            String jsonString = message.getPayload() ;
            JSONObject json = new JSONObject(jsonString);
            Long orderId = json.getLong("orderId");
            String status = json.getString("status");   // ORDER_ACCEPTED

            Optional<Order> orderOptional = orderRepository.findById(orderId) ;
            if ( orderOptional.isPresent()) {
                Order order = orderOptional.get();

                // 중복처리 방지
                if (OrderStatus.DELIVERY_ACCEPTED.getStatus().equals(status) && OrderStatus.COOKED != order.getStatus()) {
                    log.warn("### [{}] event 상태이상!!, Skip..!! : orderId={}, status={}, currentStatus={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, status, order.getStatus());
                    return ;
                } else if (OrderStatus.DELIVERY_STARTED.getStatus().equals(status) && OrderStatus.DELIVERY_ACCEPTED != order.getStatus()) {
                    log.warn("### [{}] event 상태이상!!, Skip..!! : orderId={}, status={}, currentStatus={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, status, order.getStatus());
                    return ;
                } else if (OrderStatus.DELIVERED.getStatus().equals(status) && OrderStatus.DELIVERY_STARTED != order.getStatus()) {
                    log.warn("### [{}] event 상태이상!!, Skip..!! : orderId={}, status={}, currentStatus={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, status, order.getStatus());
                    return ;
                }

                log.trace("### [{}] event received..!! : orderId={}, status={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, order.getStatus());
                order.statusUpdate(OrderStatus.valueOf(status));
                orderRepository.save(order);
            }
        } ;
    }
}
