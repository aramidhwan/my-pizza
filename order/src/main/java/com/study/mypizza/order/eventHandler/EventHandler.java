package com.study.mypizza.order.eventHandler;

import com.netflix.discovery.converters.Auto;
import com.study.mypizza.order.event.*;
import com.study.mypizza.order.eventHandler.workers.UpdateStatusWorkerThread;
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
    private UpdateStatusWorkerThread updateStatusWorker ;

    ExecutorService executorService = Executors.newCachedThreadPool() ;

//    public EventHandler(@Value("${consumer-worker.threads.whenever_OrderRejected_updateStatus}") String workerCnt) {
//        executorService = Executors.newFixedThreadPool(Integer.valueOf(workerCnt));
//    }

    @Bean
    @Transactional
    public Consumer<Message<OrderAccepted>> whenever_OrderAccepted_updateStatus() {
        return message -> {
            OrderAccepted orderAccepted = message.getPayload() ;
            if (!orderAccepted.validate()) return;

            log.debug("### Kafka Consumer start..!! :: {}", new Object(){}.getClass().getEnclosingMethod().getName()) ;

            // Multi Worker Thread 멀티 워커 쓰레드 방식
            // 작업 이후 스레드가 종료되도록 CachedThreadPool을 사용하여 스레드를 실행
            updateStatusWorker.setOrderId(orderAccepted.getOrderId());
            updateStatusWorker.setStatus(orderAccepted.getStatus());
            executorService.execute(updateStatusWorker);
        } ;
    }

    @Bean
    @Transactional
    public Consumer<Message<OrderRejected>> whenever_OrderRejected_updateStatus() {
        return message -> {
            OrderRejected orderRejected = message.getPayload() ;
            if (!orderRejected.validate()) return;

            log.debug("### Kafka Consumer start..!! :: {}", new Object(){}.getClass().getEnclosingMethod().getName()) ;

            // Multi Worker Thread 멀티 워커 쓰레드 방식
            // 작업 이후 스레드가 종료되도록 CachedThreadPool을 사용하여 스레드를 실행
            updateStatusWorker.setOrderId(orderRejected.getOrderId());
            updateStatusWorker.setStatus(orderRejected.getStatus());
            executorService.execute(updateStatusWorker);
        } ;
    }

    @Bean
    @Transactional
    public Consumer<Message<Cooked>> whenever_Cooked_updateStatus() {
        return message -> {
            Cooked cooked = message.getPayload() ;
            if (!cooked.validate()) return;

            log.debug("### Kafka Consumer start..!! :: {}", new Object(){}.getClass().getEnclosingMethod().getName()) ;

            // Multi Worker Thread 멀티 워커 쓰레드 방식
            // 작업 이후 스레드가 종료되도록 CachedThreadPool을 사용하여 스레드를 실행
            updateStatusWorker.setOrderId(cooked.getOrderId());
            updateStatusWorker.setStatus(cooked.getStatus());
            executorService.execute(updateStatusWorker);
        } ;
    }

    @Bean
    @Transactional
    public Consumer<Message<DeliveryAccepted>> whenever_DeliveryAccepted_updateStatus() {
        return message -> {
            DeliveryAccepted deliveryAccepted = message.getPayload() ;
            if (!deliveryAccepted.validate()) return;

            log.debug("### Kafka Consumer start..!! :: {}", new Object(){}.getClass().getEnclosingMethod().getName()) ;

            // Multi Worker Thread 멀티 워커 쓰레드 방식
            // 작업 이후 스레드가 종료되도록 CachedThreadPool을 사용하여 스레드를 실행
            updateStatusWorker.setOrderId(deliveryAccepted.getOrderId());
            updateStatusWorker.setStatus(deliveryAccepted.getStatus());
            executorService.execute(updateStatusWorker);
        } ;
    }

    @Bean
    @Transactional
    public Consumer<Message<DeliveryStarted>> whenever_DeliveryStarted_updateStatus() {
        return message -> {
            DeliveryStarted deliveryStarted = message.getPayload() ;
            if (!deliveryStarted.validate()) return;

            log.debug("### Kafka Consumer start..!! :: {}", new Object(){}.getClass().getEnclosingMethod().getName()) ;

            // Multi Worker Thread 멀티 워커 쓰레드 방식
            // 작업 이후 스레드가 종료되도록 CachedThreadPool을 사용하여 스레드를 실행
            updateStatusWorker.setOrderId(deliveryStarted.getOrderId());
            updateStatusWorker.setStatus(deliveryStarted.getStatus());
            executorService.execute(updateStatusWorker);
        } ;
    }

    @Bean
    @Transactional
    public Consumer<Message<Delivered>> whenever_Delivered_updateStatus() {
        return message -> {
            Delivered deliveryed = message.getPayload() ;
            if (!deliveryed.validate()) return;

            log.debug("### Kafka Consumer start..!! :: {}", new Object(){}.getClass().getEnclosingMethod().getName()) ;

            // Multi Worker Thread 멀티 워커 쓰레드 방식
            // 작업 이후 스레드가 종료되도록 CachedThreadPool을 사용하여 스레드를 실행
            updateStatusWorker.setOrderId(deliveryed.getOrderId());
            updateStatusWorker.setStatus(deliveryed.getStatus());
            executorService.execute(updateStatusWorker);
        } ;
    }
}
