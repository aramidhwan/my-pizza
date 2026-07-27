package com.study.mypizza.mypage.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.mypizza.mypage.dto.OrderDetailDto;
import com.study.mypizza.mypage.entity.MyPage;
import com.study.mypizza.mypage.enums.OrderStatus;
import com.study.mypizza.mypage.event.OrderAccepted;
import com.study.mypizza.mypage.event.OrderRejected;
import com.study.mypizza.mypage.event.Ordered;
import com.study.mypizza.mypage.event.StatusUpdated;
import com.study.mypizza.mypage.exception.MyPizzaException;
import com.study.mypizza.mypage.repository.MyPageOrderDetailRepository;
import com.study.mypizza.mypage.repository.MyPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventService {
    private final MyPageRepository myPageRepository;
    private final MyPageOrderDetailRepository myPageOrderDetailRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public void handleEvent(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        String eventType = json.getString("eventType") ;
        log.info("### kafka event received..!! : {}", jsonString);

        try {
            ObjectMapper mapper = new ObjectMapper();

            // 1. 신규 주문일 시(from Order MSA)
            if (OrderStatus.ORDERED.getStatus().equals(eventType)) {
                Ordered ordered = mapper.readValue(jsonString, Ordered.class);
                if (!ordered.validate()) return;

                if (myPageRepository.existsByOrderId(ordered.getOrderId())) {
                    String errMsg = "이미 처리된 주문 이벤트입니다. orderId={"+ ordered.getOrderId()+"}";
                    log.error(errMsg);
                    throw new MyPizzaException(errMsg) ;
                }

                MyPage myPage = modelMapper.map(ordered, MyPage.class);
                myPage.assignMyPageOrderDetail(ordered.getOrderDetailDtoList().stream()
                        .map(OrderDetailDto::toMyPageOrderDetailEntity)
                        .toList());
                myPageRepository.save(myPage) ;

            } else if ( OrderStatus.ORDER_REJECTED.getStatus().equals(eventType) ) {
                OrderRejected orderRejected = mapper.readValue(jsonString, OrderRejected.class);
                if (!orderRejected.validate()) return;

                if (myPageRepository.existsByOrderId(orderRejected.getOrderId())) {
                    String errMsg = "이미 처리된 주문 이벤트입니다. orderId={"+ orderRejected.getOrderId()+"}";
                    log.error(errMsg);
                    throw new MyPizzaException(errMsg) ;
                }

                MyPage myPage = modelMapper.map(orderRejected, MyPage.class);
                myPage.assignMyPageOrderDetail(orderRejected.getOrderDetailDtoList().stream()
                        .map(OrderDetailDto::toMyPageOrderDetailEntity)
                        .toList());
                myPageRepository.save(myPage) ;

            } else if ( OrderStatus.ORDER_ACCEPTED.getStatus().equals(eventType) ) {
                OrderAccepted orderAccepted = mapper.readValue(jsonString, OrderAccepted.class);
                if (!orderAccepted.validate()) return;

                MyPage myPage = myPageRepository.findByOrderId(orderAccepted.getOrderId())
                        .orElseThrow(()->new MyPizzaException("해당 하는 주문이 없습니다. 주문번호 : " + orderAccepted.getOrderId()));
                myPage.updateStoreId(orderAccepted.getStoreId()) ;
                myPage.updateStatus(orderAccepted.getStatus(), orderAccepted.getStatusInfo());
                myPageRepository.save(myPage) ;

            } else if ( "StatusUpdated".equals(eventType) ) {
                StatusUpdated statusUpdated = mapper.readValue(jsonString, StatusUpdated.class);
                if (!statusUpdated.validate()) return;

                MyPage myPage = myPageRepository.findByOrderId(statusUpdated.getOrderId())
                        .orElseThrow(()->new MyPizzaException("해당 하는 주문이 없습니다. 주문번호 : " + statusUpdated.getOrderId()));
                myPage.updateStatus(statusUpdated.getStatus(), statusUpdated.getStatusInfo());
                myPageRepository.save(myPage) ;
            } else {
                log.warn("### kafka UNKNOWN event received..!! : {}", eventType);
            }
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
