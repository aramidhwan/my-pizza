package com.study.mypizza.customercenter.eventHandler;

import com.study.mypizza.customercenter.entity.MyPage;
import com.study.mypizza.customercenter.event.Ordered;
import com.study.mypizza.customercenter.event.StatusUpdated;
import com.study.mypizza.customercenter.repository.MyPageRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Service
@Slf4j
public class EventHandler {
    @Autowired
    private MyPageRepository myPageRepository;

    ExecutorService executorService = Executors.newCachedThreadPool() ;

//    public EventHandler(@Value("${consumer-worker.threads.whenever_OrderRejected_updateStatus}") String workerCnt) {
//        executorService = Executors.newFixedThreadPool(Integer.valueOf(workerCnt));
//    }

    @Bean
    @Transactional
    public Consumer<Message<Ordered>> whenever_Ordered_CreateMyPage() {
        return message -> {
            Ordered ordered = message.getPayload() ;
            if (!ordered.validate()) return;

            log.debug("### Kafka Consumer start..!! :: {}", new Object(){}.getClass().getEnclosingMethod().getName()) ;

            MyPage myPage = new MyPage() ;
            BeanUtils.copyProperties(ordered, myPage);
            myPageRepository.save(myPage) ;
        } ;
    }

    @Bean
    @Transactional
    public Consumer<Message<StatusUpdated>> whenever_OrderStatusUpdated_modifyMyPage() {
        return message -> {
            StatusUpdated statusUpdated = message.getPayload() ;
            if (!statusUpdated.validate()) return;

            log.debug("### Kafka Consumer start..!! :: {}", new Object(){}.getClass().getEnclosingMethod().getName()) ;

            Optional<MyPage> optional = myPageRepository.findByOrderId(statusUpdated.getOrderId());
            if (optional.isPresent()) {
                MyPage myPage = optional.get() ;
                myPage.setStatus(statusUpdated.getStatus());
                myPageRepository.save(myPage);
            }
        } ;
    }
}
