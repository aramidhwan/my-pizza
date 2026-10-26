package com.study.mypizza.mypage.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.mypizza.mypage.dto.OrderDetailDto;
import com.study.mypizza.mypage.entity.MyPage;
import com.study.mypizza.mypage.entity.PendingEvent;
import com.study.mypizza.mypage.enums.OrderStatus;
import com.study.mypizza.mypage.repository.MyPageRepository;
import com.study.mypizza.mypage.repository.PendingEventRepository;
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
    private final MyPageRepository myPageRepository;
    private final PendingEventRepository pendingEventRepository;

    @Transactional
    public void handleEvent(String payload) {
        JSONObject json = new JSONObject(payload);
        String eventType = json.optString("eventType") ;
        String status = json.optString("status") ;

        // ORDER_CANCELLED 는 MyPage MSA에서 자체 발행한 이벤트를 Order MSA가 다시 전파한 것이므로 Skip!!
        if ( OrderStatus.ORDER_CANCELLED == OrderStatus.valueOf(status) ) {
            return;
        }

        log.info("👉 카프카 이벤트 수신..!! : {}, orderId=[{}], {}", String.format("%-20s", status), json.optLong("orderId"), payload);

        // 2. 신규 주문 시 new MyPage 생성
        if (OrderStatus.ORDERED == OrderStatus.valueOf(status) ) {
            // new MyPage 생성
            boolean isSucceeded = createMyPage(payload) ;
            if (!isSucceeded) return;

            // kafka 순서 역전된 pendingEvent가 있으면 처리
            processPendingEvent(json);

        // 1. ORDER_ACCEPTED, ORDER_REJECTED, COOKED, DELIVERY_ACCEPTED 등 일 경우 상태(STATUS) 업데이트
        } else if ( OrderStatus.isValid(status) ) {
            boolean isSucceeded = updateStatus(payload) ;
            if (isSucceeded) {
                // kafka 순서 역전된 pendingEvent가 있으면 처리
                processPendingEvent(json);
            } else {
                createPendingEvent(payload);
            }
        } else {
            log.error("⚠️ [ERROR]️️️ 카프카 UNKNOWN 이벤트 수신..!! : orderId=[{}], status={}", json.optLong("orderId"), eventType);
        }
    }

    private boolean createMyPage(String payload) {
        JSONObject json = new JSONObject(payload);
        String eventType = json.optString("eventType") ;
        
        // kafka 중복 이벤트 방지
        if (myPageRepository.existsByOrderId(json.optLong("orderId"))) {
            log.warn("👉 마이페이지 테이블(t_mypage)에 이미 등록된 주문 이벤트입니다. orderId=[{}], eventType={}", json.optLong("orderId"), eventType);
            return false;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // payload 에서 복사 대상 Class로 필드명 매핑되지 않는 필드가 있어도 예외를 던지지 않도록 설정
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    
            // JSONObject 생성을 거쳤다면 String 으로 다시 추출
            String arrayString = json.getJSONArray("orderDetailDtoList").toString();
            // Jackson ObjectMapper 로 List<OrderDetailDto> 자동 파싱
            List<OrderDetailDto> detailList = objectMapper.readValue(arrayString, new TypeReference<>() {});
            MyPage myPage = objectMapper.readValue(payload, MyPage.class);
            myPage.assignMyPageOrderDetail(detailList.stream()
                    .map(OrderDetailDto::toMyPageOrderDetailEntity)
                    .toList());
            myPageRepository.save(myPage);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return true;
    }

    private boolean updateStatus(String payload) {
        JSONObject json = new JSONObject(payload);
        long orderId = json.optLong("orderId");
        String nextStatus = json.optString("status") ;
        String statusInfo = json.optString("statusInfo") ;

        Optional<MyPage> optional = myPageRepository.findByOrderId(orderId);
        if (optional.isEmpty()) {
            log.warn("👉 마이페이지 테이블(t_mypage)에 해당 하는 주문이 없습니다. orderId=[{}], 요청상태 : {}", orderId, nextStatus);
            return false ;
        }

        MyPage myPage = optional.get();

        boolean isSucceeded = myPage.updateStatus(OrderStatus.valueOf(nextStatus), statusInfo);
        if (!isSucceeded) {
            return false;
        }

        // 상점의 주문접수 시 storeId 셋팅
        if (OrderStatus.ORDER_ACCEPTED == OrderStatus.valueOf(nextStatus)) {
            myPage.setStoreId(json.optLong("storeId"));
            // 배달점의 배달접수 시 deliveryId 셋팅
        } else if (OrderStatus.DELIVERY_ACCEPTED == OrderStatus.valueOf(nextStatus)) {
            myPage.setDeliveryId(json.optLong("deliveryId"));
        }

        return true;
    }

    private void createPendingEvent(String payload) {
        JSONObject json = new JSONObject(payload);
        Long orderId = json.optLong("orderId") ;
        String status = json.optString("status") ;
        Optional<MyPage> optional = myPageRepository.findByOrderId(orderId) ;
        // 주문건의 STATUS 가 종결 상태인 경우 pendingEvent 미등록
        if (optional.isPresent()) {
            MyPage mypage = optional.get() ;
            if (OrderStatus.isTerminatedStatus(mypage.getStatus())) {
                log.warn("👉 STATUS 종결 상태! t_pending_event 테이블에 저장 안함. orderId=[{}], 현재상태 : {}, 요청상태 : {}", orderId, mypage.getStatus(), json.optString("status"));
                return;
            }
        }

        // kafka event 순서 역전임! t_pending_event 테이블에 등록(Transaction inbox)
        PendingEvent pendingEvent = PendingEvent.builder()
                .orderId(json.optLong("orderId"))
                .msaGb("MY_PAGE")
                .eventType(json.optString("eventType"))
                .status(OrderStatus.valueOf(status))
                .payload(payload)
                .isCompleted(false)
                .build();
        pendingEventRepository.save(pendingEvent);
        log.warn("👉 카프카 메시지 순서 역전! t_pending_event 테이블에 저장함. orderId=[{}], 요청상태 : {}", json.optLong("orderId"), status);
    }

    private void processPendingEvent(JSONObject json) {
        // kafka 순서 역전된 pendingEvent가 있으면 처리
        List<PendingEvent> pendingEventList = pendingEventRepository.findByOrderIdAndMsaGbAndIsCompletedFalseOrderByEventId(json.optLong("orderId"), "MY_PAGE") ;
        int maxRetry = 0 ;
        while (!pendingEventList.isEmpty() && maxRetry < 5) {
            maxRetry++ ;
            for (PendingEvent pendingEvent : pendingEventList) {
                log.info("👉 PendingEvent 처리 시작! ({}회). orderId=[{}], status={}, payload={}", maxRetry, pendingEvent.getOrderId(), pendingEvent.getStatus(), pendingEvent.getPayload()) ;
                // pendingEvent 처리 성공 시 "완료" 처리
                boolean isSucceeded = updateStatus(pendingEvent.getPayload());
                if (isSucceeded) {
                    pendingEvent.setCompleted(true);
                    log.info("👉 PendingEvent 처리 성공(완료)!({}회). orderId=[{}], status={}, payload={}", maxRetry, pendingEvent.getOrderId(), pendingEvent.getStatus(), pendingEvent.getPayload()) ;
                }
            }
            pendingEventList = pendingEventRepository.findByOrderIdAndMsaGbAndIsCompletedFalseOrderByEventId(json.optLong("orderId"), "MY_PAGE") ;
        }
    }
}
