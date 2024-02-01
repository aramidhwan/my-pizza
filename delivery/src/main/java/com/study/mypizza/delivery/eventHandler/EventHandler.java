package com.study.mypizza.delivery.eventHandler;

import com.study.mypizza.delivery.event.Cooked;
import com.study.mypizza.delivery.eventHandler.workers.DeliveryAcceptWorkerThread;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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
    @Autowired
    private DeliveryAcceptWorkerThread deliveryAcceptWorker;

    ExecutorService executorService = Executors.newCachedThreadPool() ;

//    public EventHandler(@Value("${consumer-worker.threads.whenever_OrderRejected_updateStatus}") String workerCnt) {
//        executorService = Executors.newFixedThreadPool(Integer.valueOf(workerCnt));
//    }

    @Bean
    @Transactional
    public Consumer<Message<Cooked>> whenever_Cooked_DeliveryAccept() {
        return message -> {
            Cooked cooked = message.getPayload() ;
            if (!cooked.validate()) return;

            log.debug("### Kafka Consumer start..!! :: {}", new Object(){}.getClass().getEnclosingMethod().getName()) ;

            // Multi Worker Thread 멀티 워커 쓰레드 방식
            // 작업 이후 스레드가 종료되도록 CachedThreadPool을 사용하여 스레드를 실행
            deliveryAcceptWorker.setCooked(cooked);
            executorService.execute(deliveryAcceptWorker);
        } ;
    }
}
