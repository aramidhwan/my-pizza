package com.study.mypizza.customercenter.eventHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.mypizza.customercenter.entity.MyPage;
import com.study.mypizza.customercenter.entity.MyPageOrderDetail;
import com.study.mypizza.customercenter.enums.OrderStatus;
import com.study.mypizza.customercenter.event.*;
import com.study.mypizza.customercenter.exception.MyPizzaException;
import com.study.mypizza.customercenter.repository.MyPageOrderDetailRepository;
import com.study.mypizza.customercenter.repository.MyPageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventHandler {
//    @Autowired
//    CreateMyPageWorkerThread createMyPageWorkerThread ;
//    @Autowired
//    ModifyMyPageWorkerThread modifyMyPageWorkerThread ;

    private final MyPageRepository myPageRepository;
    private final MyPageOrderDetailRepository myPageOrderDetailRepository;
//    ExecutorService executorService ;

//    public EventHandler(@Value("${consumer-worker.threads}") String workerCnt) {
//        executorService = Executors.newFixedThreadPool(Integer.valueOf(workerCnt));
//    }

    @Bean
    @Transactional
    public Consumer<Message<String>> whenever_OrderedEvent_CreateModifyMyPage() {
        return eventMessage -> {
            String jsonString = eventMessage.getPayload() ;
            JSONObject json = new JSONObject(jsonString);
            String eventType = json.getString("eventType") ;

            try {
                ObjectMapper mapper = new ObjectMapper();

                if (OrderStatus.ORDERED.getStatus().equals(eventType)) {
                    Ordered ordered = mapper.readValue(jsonString, Ordered.class);
                    log.trace("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), ordered.toString());

                    ModelMapper modelMapper = new ModelMapper();
//                    modelMapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
                    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

                    MyPage myPage = modelMapper.map(ordered, MyPage.class);
                    myPageRepository.save(myPage) ;

                } else if ( OrderStatus.ORDER_ACCEPTED.getStatus().equals(eventType) ) {
                    OrderAccepted orderAccepted = mapper.readValue(jsonString, OrderAccepted.class);
                    if (!orderAccepted.validate()) return;

                    log.trace("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), orderAccepted.toString());

                    MyPage myPage = myPageRepository.findByOrderId(orderAccepted.getOrderId())
                            .orElseThrow(()->new MyPizzaException("해당 하는 주문이 없습니다. 주문번호 : " + orderAccepted.getOrderId()));
                    myPage.updateStoreId(orderAccepted.getStoreId()) ;
                    myPage.updateStatus(orderAccepted.getStatus(), orderAccepted.getStatusInfo());
                    myPageRepository.save(myPage) ;

                } else if ( OrderStatus.ORDER_REJECTED.getStatus().equals(eventType) ) {
                    OrderRejected orderRejected = mapper.readValue(jsonString, OrderRejected.class);
                    if (!orderRejected.validate()) return;

                    log.trace("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), orderRejected.toString());

                    ModelMapper modelMapper = new ModelMapper();
//                    modelMapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
                    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

                    MyPage myPage = modelMapper.map(orderRejected, MyPage.class);
                    myPageRepository.save(myPage) ;

                } else if ( "OrderDetailOrdered".equals(eventType) ) {
                    OrderDetailOrdered orderDetailOrdered = mapper.readValue(jsonString, OrderDetailOrdered.class);
                    if (!orderDetailOrdered.validate()) return;
                    log.trace("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), orderDetailOrdered.toString());

                    MyPageOrderDetail myPageOrderDetail = MyPageOrderDetail.builder()
                            .orderDetailId(orderDetailOrdered.getOrderDetailId())
                            .orderId(orderDetailOrdered.getOrderId())
                            .itemId(orderDetailOrdered.getItemId())
                            .qty(orderDetailOrdered.getQty())
                            .pricePerOne(orderDetailOrdered.getPricePerOne())
                            .build();;
                    myPageOrderDetailRepository.save(myPageOrderDetail) ;
                } else if ( "StatusUpdated".equals(eventType) ) {
                    StatusUpdated statusUpdated = mapper.readValue(jsonString, StatusUpdated.class);
                    if (!statusUpdated.validate()) return;
                    log.trace("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), statusUpdated.toString());

                    MyPage myPage = myPageRepository.findByOrderId(statusUpdated.getOrderId())
                            .orElseThrow(()->new MyPizzaException("해당 하는 주문이 없습니다. 주문번호 : " + statusUpdated.getOrderId()));
                    myPage.updateStatus(statusUpdated.getStatus(), statusUpdated.getStatusInfo());
                    myPageRepository.save(myPage) ;
                } else {
                    log.warn("### [{}] etc event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), eventType);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            // Multi Worker Thread 멀티 워커 쓰레드 방식
            // 작업 이후 스레드가 종료되도록 CachedThreadPool을 사용하여 스레드를 실행
//            createMyPageWorkerThread.setOrdered(ordered);
//            executorService.execute(createMyPageWorkerThread);
        } ;
    }
//
//    @Bean
//    @Transactional
//    public Consumer<Message<OrderRejected>> whenever_OrderRejected_CreateMyPage() {
//        return eventMessage -> {
//
//            OrderRejected orderRejected = eventMessage.getPayload() ;
//            if (!orderRejected.validate()) return;
//
//            log.debug("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), orderRejected.toString());
//
//            MyPage myPage = new MyPage() ;
//            BeanUtils.copyProperties(orderRejected, myPage);
//            myPageRepository.save(myPage) ;
//        } ;
//    }
//
//    @Bean
//    @Transactional
//    public Consumer<Message<StatusUpdated>> whenever_OrderStatusUpdated_modifyMyPage() {
//        return eventMessage -> {
//            StatusUpdated statusUpdated = eventMessage.getPayload() ;
//            if (!statusUpdated.validate()) return;
//            log.debug("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), statusUpdated.toString());
//
//            Optional<MyPage> optional = myPageRepository.findByOrderId(statusUpdated.getOrderId());
//            if (optional.isPresent()) {
//                MyPage myPage = optional.get();
//                myPage.setStatus(statusUpdated.getStatus());
//                myPageRepository.save(myPage);
//            }

            // Multi Worker Thread 멀티 워커 쓰레드 방식
            // 작업 이후 스레드가 종료되도록 CachedThreadPool을 사용하여 스레드를 실행
//            modifyMyPageWorkerThread.setStatusUpdated(statusUpdated);
//            executorService.execute(modifyMyPageWorkerThread);
//        } ;
//    }

//    @Bean
//    @Transactional
//    public Consumer<Message<String>> whenever_OrderStatusUpdated_modifyMyPage() {
//        return eventMessage -> {
//            String jsonString = eventMessage.getPayload() ;
//            JSONObject json = new JSONObject(jsonString);
//            String event = json.getString("eventType");
//            Class eventClass  = Class.forName("com.study.mypizza.customercenter.event."+event);
//
//
//            Long orderId = json.getLong("orderId");
//            String status = json.getString("status");
//
//            if (!statusUpdated.validate()) return;
//
//            log.debug("### Kafka Consumer start..!! :: {}", new Object(){}.getClass().getEnclosingMethod().getName()) ;
//
//            // Multi Worker Thread 멀티 워커 쓰레드 방식
//            // 작업 이후 스레드가 종료되도록 CachedThreadPool을 사용하여 스레드를 실행
//            modifyMyPageWorkerThread.setStatusUpdated(statusUpdated);
//            executorService.execute(modifyMyPageWorkerThread);
//        } ;
//    }
}
