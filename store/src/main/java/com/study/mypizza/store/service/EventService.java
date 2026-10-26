package com.study.mypizza.store.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.mypizza.store.entity.PendingEvent;
import com.study.mypizza.store.entity.Store;
import com.study.mypizza.store.entity.StoreOrder;
import com.study.mypizza.store.enums.OrderStatus;
import com.study.mypizza.store.event.AbstractEvent;
import com.study.mypizza.store.event.OrderRejected;
import com.study.mypizza.store.event.Ordered;
import com.study.mypizza.store.exception.MyPizzaException;
import com.study.mypizza.store.external.InternalGateway;
import com.study.mypizza.store.repository.PendingEventRepository;
import com.study.mypizza.store.repository.StoreOrderRepository;
import com.study.mypizza.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventService {
    private final StoreRepository storeRepository;
    private final StoreOrderRepository storeOrderRepository;
    private final InternalGateway internalGateway;
    private final PendingEventRepository pendingEventRepository;

    @Transactional
    public void handleOrderedEvent(String payload) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Ordered ordered = objectMapper.readValue(payload, Ordered.class);

            log.info("👉 카프카 이벤트 수신..!! : {}, orderId=[{}], {}", String.format("%-20s", ordered.getStatus()), ordered.getOrderId(), ordered);

            boolean isSucceeded = createStoreOrder(ordered);
            if (!isSucceeded) return;

            // kafka 순서 역전된 pendingEvent가 있으면 처리
            processPendingEvent(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private boolean createStoreOrder(Ordered ordered) {
        // 카프카 메시지 중복 수신 체크
        Optional<StoreOrder> optional = storeOrderRepository.findByOrderId(ordered.getOrderId());
        if (optional.isPresent()) {
            log.warn("👉 상점주문(t_store_order)에 이미 등록된 주문 이벤트입니다! orderId=[{}]", ordered.getOrderId());
            return false;
        }

        List<Store> storeList = storeRepository.findByRegionNmAndOpenYNTrue(ordered.getRegionNm());
        int openStoreCnt = storeList.size();
        // 디버깅용 로그: 실제로 몇 개의 상점이 조회되었는지 확인
        log.debug("👉 주문 지역: {}, 영업 중인 상점 수: {}", ordered.getRegionNm(), openStoreCnt);

        // 주문이 들어온 regionNm에 Open된 Sotre가 한군데라도 있으면 주문접수
        if (openStoreCnt > 0) {
            // 1. 주문 랜덤 배정, ThreadLocalRandom 사용 (성능 최적화)
            int random = java.util.concurrent.ThreadLocalRandom.current().nextInt(openStoreCnt);

            // 2. 랜덤으로 선택된 상점을 변수로 추출 (가독성 및 효율성 향상)
            Store selectedStore = storeList.get(random);

            OrderStatus initStatus = null;
            if ("AUTO_ACCEPT".equals(selectedStore.getOrderAcceptMode())) {
                initStatus = OrderStatus.ORDER_ACCEPTED;
            } else if ("MANUAL_ACCEPT".equals(selectedStore.getOrderAcceptMode())) {
                initStatus = OrderStatus.ORDERED;
            } else {
                log.error("⚠️ {}(StoreId={})의 주문접수 모드(orderAcceptMode)를 알 수 없습니다. {}",
                        selectedStore.getStoreNm(),
                        selectedStore.getStoreId(),
                        selectedStore.getOrderAcceptMode()
                );
                throw new MyPizzaException("⚠️ [" + selectedStore.getStoreNm() + "]의 주문접수 모드(orderAcceptMode)를 알 수 없습니다.");
            }

            StoreOrder storeOrder = StoreOrder.builder()
                    .store(Store.builder().storeId(selectedStore.getStoreId()).build())
                    .orderId(ordered.getOrderId())
                    .status(initStatus)
                    .build();
            storeOrder.assignStoreOrderDetail(ordered.getOrderDetailDtoList());
            storeOrderRepository.save(storeOrder);

            // ORDERED 건에 대해 ORDER MSA 에서 주문 할당된 storeId 셋팅
            internalGateway.assignStoreId(storeOrder.getOrderId(), storeOrder.getStore().getStoreId()) ;
        } else {
            log.error("⚠️ 지역구 에 영업중인 상점이 없습니다! orderId=[{}], regionNm={}", ordered.getOrderId(), ordered.getRegionNm());
            ordered.setStatus(OrderStatus.ORDER_REJECTED);
            publishEvent(ordered);
            return false;
            // Exception 발생 시에는 kafka event recieve 처리가 max-attmpts 만큼 재시도 되므로 Exception을 발생시키면 안된다.
        }

        return true;
    }

    /**
     * 주문취소(ORDER_CANCELLED) 처리
     * @param payload
     */
    @Transactional
    public void handleOrderCancelledEvent(String payload) {
        JSONObject json = new JSONObject(payload);
        Long orderId = json.optLong("orderId") ;
        String nextStatus = json.optString("status") ;

        log.info("👉 카프카 이벤트 수신..!! : {}, orderId=[{}], {}", String.format("%-20s", nextStatus), orderId, payload);

        // 상태 업데이트 (ORDER_CANCELLED 처리)
        boolean isSucceeded = updateStatus(payload) ;
        if (isSucceeded) {
            // kafka 순서 역전된 pendingEvent가 있으면 처리
            processPendingEvent(payload);
        } else {
            createPendingEvent(payload);
        }
    }

    private boolean updateStatus(String payload) {
        JSONObject json = new JSONObject(payload);
        Long orderId = json.optLong("orderId") ;
        String nextStatus = json.optString("status") ;
        // 카프카 메시지 순서 역전 체크
        Optional<StoreOrder> optional = storeOrderRepository.findByOrderId(orderId);
        if (optional.isEmpty()) {
            log.warn("👉 상점주문(t_store_order)에 해당 하는 주문이 없습니다. orderId=[{}], 요청상태 : {}", orderId, nextStatus);
            return false;
        }

        StoreOrder storeOrder = optional.get() ;
        return storeOrder.updateStatus(OrderStatus.valueOf(nextStatus));
    }

    private void createPendingEvent(String payload) {
        JSONObject json = new JSONObject(payload);
        Long orderId = json.optLong("orderId") ;
        String status = json.optString("status") ;
        Optional<StoreOrder> optional = storeOrderRepository.findByOrderId(orderId) ;
        // 주문건의 STATUS 가 종결 상태인 경우 pendingEvent 미등록
        if (optional.isPresent()) {
            StoreOrder storeOrder = optional.get() ;
            if (OrderStatus.isTerminatedStatus(storeOrder.getStatus())) {
                log.warn("👉 STATUS 종결 상태! t_pending_event 테이블에 저장 안함. orderId=[{}], 현재상태 : {}, 요청상태 : {}", orderId, storeOrder.getStatus(), json.optString("status"));
                return;
            }
        }

        // kafka event 순서 역전임! t_pending_event 테이블에 등록(Transaction inbox)
        PendingEvent pendingEvent = PendingEvent.builder()
                .orderId(json.optLong("orderId"))
                .msaGb("STORE")
                .eventType(json.optString("eventType"))
                .status(OrderStatus.valueOf(status))
                .payload(payload)
                .isCompleted(false)
                .build();
        pendingEventRepository.save(pendingEvent);
        log.warn("👉 카프카 메시지 순서 역전! t_pending_event 테이블에 저장함. orderId=[{}], 요청상태 : {}", json.optLong("orderId"), status);
    }

    private void processPendingEvent(String payload) {
        JSONObject json = new JSONObject(payload);
        // kafka 순서 역전된 pendingEvent가 있으면 처리
        List<PendingEvent> pendingEventList = pendingEventRepository.findByOrderIdAndMsaGbAndIsCompletedFalseOrderByEventId(json.optLong("orderId"), "STORE") ;
        int maxRetry = 0 ;
        while (!pendingEventList.isEmpty() && maxRetry < 5) {
            maxRetry++ ;
            for (PendingEvent pendingEvent : pendingEventList) {
                log.info("👉 PendingEvent 처리 시작! ({}회). orderId=[{}], status={}, payload={}", maxRetry, pendingEvent.getOrderId(), String.format("%-20s", pendingEvent.getStatus()), pendingEvent.getPayload()) ;
                // pendingEvent 처리 성공 시 "완료" 처리
                boolean isSucceeded = updateStatus(pendingEvent.getPayload());
                if (isSucceeded) {
                    pendingEvent.setCompleted(true);
                    log.info("👉 PendingEvent 처리 성공!({}회). orderId=[{}], status={}, payload={}", maxRetry, pendingEvent.getOrderId(), String.format("%-20s", pendingEvent.getStatus()), pendingEvent.getPayload()) ;
                }
            }
            pendingEventList = pendingEventRepository.findByOrderIdAndMsaGbAndIsCompletedFalseOrderByEventId(json.optLong("orderId"), "STORE") ;
        }
    }
    // kafka 이벤트(Ordered) 발행 (ORDER_REJECTED)
    private void publishEvent(AbstractEvent ordered) {
        OrderRejected event = new OrderRejected();
        BeanUtils.copyProperties(ordered, event);

        // 5. 현재 DB 트랜잭션 커밋 이후 한 번만 발행
        event.publishAfterCommit();
        log.info("👉 카프카 이벤트 발송..!! {}", event);
    }

}
