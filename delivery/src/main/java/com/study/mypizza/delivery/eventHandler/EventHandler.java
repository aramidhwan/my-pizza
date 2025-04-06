package com.study.mypizza.delivery.eventHandler;

import com.study.mypizza.delivery.entity.Delivery;
import com.study.mypizza.delivery.enums.OrderStatus;
import com.study.mypizza.delivery.event.Cooked;
import com.study.mypizza.delivery.event.StatusUpdated;
import com.study.mypizza.delivery.eventHandler.workers.DeliveryAcceptWorkerThread;
import com.study.mypizza.delivery.repository.DeliveryRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Service
@Slf4j
public class EventHandler {
//    @Autowired
//    private DeliveryAcceptWorkerThread deliveryAcceptWorker;
    @Autowired
    private DeliveryRepository deliveryRepository;

//    ExecutorService executorService = Executors.newCachedThreadPool() ;

//    public EventHandler(@Value("${consumer-worker.threads.whenever_OrderRejected_updateStatus}") String workerCnt) {
//        executorService = Executors.newFixedThreadPool(Integer.valueOf(workerCnt));
//    }

    @Bean
    @Transactional
    public Consumer<Message<StatusUpdated>> whenever_Cooked_DeliveryAccept() {
        return message -> {
            StatusUpdated statusUpdated = message.getPayload() ;
            if (!statusUpdated.validate()) return;
            if (OrderStatus.COOKED != statusUpdated.getStatus()) return;

            log.trace("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), statusUpdated.toString());

            // Multi Worker Thread 멀티 워커 쓰레드 방식
            // 작업 이후 스레드가 종료되도록 CachedThreadPool을 사용하여 스레드를 실행
//            deliveryAcceptWorker.setStatusUpdated(statusUpdated);
//            executorService.execute(deliveryAcceptWorker);

            Delivery delivery = Delivery.builder()
                    .orderId(statusUpdated.getOrderId())
                    .storeId(statusUpdated.getStoreId())
                    .status(OrderStatus.DELIVERY_ACCEPTED)
                    .ownerNo(7)
                    .build();
            deliveryRepository.save(delivery);
        } ;
    }
}
