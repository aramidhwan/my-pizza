package com.study.mypizza.order.service;

import com.study.mypizza.order.entity.Order;
import com.study.mypizza.order.entity.PendingEvent;
import com.study.mypizza.order.enums.OrderStatus;
import com.study.mypizza.order.repository.OrderRepository;
import com.study.mypizza.order.repository.PendingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventService {
    private final CacheService cacheService;
    private final OrderRepository orderRepository;
    private final PendingEventRepository pendingEventRepository;

    /**
     * STORE MSA 발생 event를 처리한다.
     * 접수이벤트 : 주문접수(ORDER_ACCEPTED), 조리완료(COOKED)
     *            배달접수(DELIVERY_ACCEPTED), 배달시작(DELIVERY_STARTED), 배달완료(DELIVERED)
     * TO DO : t_order의 해당 orderId 데이터 상태(status) 업데이트
     *          ORDER_ACCEPTED인 경우 주문접수한 storeId 셋팅
     *          DELIVERY_ACCEPTED인 경우 주문접수한 storeId 셋팅
     * @param payload (json)
     */
    @Transactional
    public void handleEvent(String payload) {
        JSONObject json = new JSONObject(payload);
        String eventType = json.optString("eventType") ;  // "OrderAccepted"
        String status = json.optString("status") ;  // "OrderAccepted"
        Long orderId = json.optLong("orderId");
        log.info("👉 카프카 이벤트 수신..!! : {}, orderId={}, {}", String.format("%-20s", status), orderId, payload);

        // 상점(Store) 정보 업데이트 이벤트 --> 캐시 삭제
        if ("StoreUpdated".equals(eventType)) {
            cacheService.clearOpenRegionNmListCache();
            cacheService.clearAllOpenYnCache() ;
        // 그 외 이벤트들(STATUS 업데이트)
        } else {
            // 주문 상태(STATUS) 업데이트
            boolean isSucceeded = updateStatus(payload);
            // 성공 시 kafka 순서 역전된 pendingEvent가 있으면 처리
            if (isSucceeded) {
                processPendingEvent(json);
                // 실패 시 kafka event 순서 역전임! t_pending_event 테이블에 등록(Transaction inbox)
            } else {
                createPendingEvent(payload);
            }
        }
    }

    private boolean updateStatus(String payload) {
        JSONObject json = new JSONObject(payload);
        long orderId = json.optLong("orderId");
        String toBeStatus = json.optString("status") ;

        // 기존 주문(Order) 검색
        Optional<Order> orderOptional = orderRepository.findById(orderId) ;
        if (orderOptional.isEmpty()) {
            log.error("⚠️ 주문 테이블(t_order)에 해당 하는 주문이 없습니다! 주문번호 : {}", orderId);
            return false ;
        }

        Order order = orderOptional.get();

        // 상태 업데이트
        boolean isSucceeded = order.updateStatus(OrderStatus.valueOf(toBeStatus));
        if (!isSucceeded) {
            return false;
        }

        // ORDER_ACCEPTED, ORDER_REJECTED 인 경우 storeId 셋팅
        if ( OrderStatus.ORDER_ACCEPTED == OrderStatus.valueOf(toBeStatus)
             || OrderStatus.ORDER_REJECTED == OrderStatus.valueOf(toBeStatus) ) {
            order.assignStore(json.optLong("storeId"));
            // 배달접수(DELIVERY_ACCEPTED) 인 경우 배달점(deliveryId) 셋팅
        } else if (OrderStatus.DELIVERY_ACCEPTED == OrderStatus.valueOf(toBeStatus)) {
            order.assignDelivery(json.optLong("deliveryId"));
        }

        return true;
    }

    /**
     * 카프카 메시지 순서 역전되어 t_pending_event 테이블에 등록된 payload가 존재하면 처리.
     * @param json
     */
    private void processPendingEvent(JSONObject json) {
        // kafka 순서 역전된 pendingEvent가 있으면 처리
        List<PendingEvent> pendingEventList = pendingEventRepository.findByOrderIdAndMsaGbAndIsCompletedFalseOrderByEventId(json.optLong("orderId"), "ORDER") ;
        int maxRetry = 0 ;
        while (!pendingEventList.isEmpty() && maxRetry < 5) {
            maxRetry++ ;
            for (PendingEvent pendingEvent : pendingEventList) {
                log.info("👉 PendingEvent 처리 시작! ({}회). orderId={}, status={}, payload={}", maxRetry, pendingEvent.getOrderId(), String.format("%-20s", pendingEvent.getStatus()), pendingEvent.getPayload()) ;
                // pendingEvent 처리 성공 시 "완료" 처리
                boolean isSucceeded = updateStatus(pendingEvent.getPayload());
                if (isSucceeded) {
                    pendingEvent.setCompleted(true);
                    log.info("👉 PendingEvent 처리 성공(완료)!({}회). orderId={}, status={}, payload={}", maxRetry, pendingEvent.getOrderId(), String.format("%-20s", pendingEvent.getStatus()), pendingEvent.getPayload()) ;
                }
            }
            pendingEventList = pendingEventRepository.findByOrderIdAndMsaGbAndIsCompletedFalseOrderByEventId(json.optLong("orderId"), "ORDER") ;
        }
    }

    /**
     * 카프카 메시지 순서 역전 현상이 발생한 경우 t_pending_event 테이블에 이벤트(payload) 저장
     * @param payload
     */
    private void createPendingEvent(String payload) {
        JSONObject json = new JSONObject(payload);
        Long orderId = json.optLong("orderId") ;
        Optional<Order> optional = orderRepository.findById(orderId) ;
        // 주문건의 STATUS 가 종결 상태인 경우 pendingEvent 미등록
        if (optional.isPresent()) {
            Order order = optional.get() ;
            if (OrderStatus.isTerminatedStatus(order.getStatus())) {
                log.warn("👉 STATUS 종결 상태! t_pending_event_order 테이블에 저장 안함. orderId=[{}], 현재상태 : {}, 요청상태 : {}", orderId, order.getStatus(), json.optString("status"));
                return;
            }
        }

        // pendingEvent 등록
        PendingEvent pendingEvent = PendingEvent.builder()
                .orderId(json.optLong("orderId"))
                .msaGb("ORDER")
                .eventType(json.optString("eventType"))
                .status(OrderStatus.valueOf(json.optString("status")))
                .payload(payload)
                .isCompleted(false)
                .build();
        pendingEventRepository.save(pendingEvent);
        log.warn("👉 카프카 메시지 순서 역전! t_pending_event_order 테이블에 저장함. orderId=[{}], 상태 : {}", json.optLong("orderId"), json.optString("status"));
    }
}
