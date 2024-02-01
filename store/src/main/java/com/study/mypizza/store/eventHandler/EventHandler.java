package com.study.mypizza.store.eventHandler;

import com.study.mypizza.store.event.Ordered;
import com.study.mypizza.store.eventHandler.workers.OrderAcceptWorkerThread;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    OrderAcceptWorkerThread orderAcceptWorker ;

    ExecutorService executorService ;

    public EventHandler(@Value("${consumer-worker.threads.order-accept.count}") String workerCnt) {
        executorService = Executors.newFixedThreadPool(Integer.valueOf(workerCnt));
    }

    @Bean
    @Transactional
    public Consumer<Message<Ordered>> whenever_ordered_orderAccept() {
        return orderedEventMessage -> {
            Ordered ordered = orderedEventMessage.getPayload() ;
            if (!ordered.validate()) return;

            log.debug("### Kafka Consumer start..!! :: {}", new Object(){}.getClass().getEnclosingMethod().getName()) ;

            // Multi Worker Thread 멀티 워커 쓰레드 방식
            // 작업 이후 스레드가 종료되도록 CachedThreadPool을 사용하여 스레드를 실행
            orderAcceptWorker.setOrdered(ordered);
//            executorService.execute(worker);
            new Thread(orderAcceptWorker).start();
        } ;
    }
}
