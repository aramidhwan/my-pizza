package com.study.mypizza.mypage.eventHandler.workers;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Setter
@Slf4j
public class CreateMyPageWorkerThread implements Runnable {
//
//    @Autowired
//    MyPageRepository myPageRepository;
//    private Ordered ordered;
//    private OrderRejected orderRejected;
//
    @Override
    public void run() {
//        createMyPage();
    }
//
//    private void createMyPage() {
//
//        MyPage myPage = new MyPage() ;
//        if ( ordered != null ) {
//            log.debug("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), ordered.toString());
//            BeanUtils.copyProperties(ordered, myPage);
//        } else if ( orderRejected != null ) {
//            log.debug("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), orderRejected.toString());
//            BeanUtils.copyProperties(orderRejected, myPage);
//        }
//        myPageRepository.save(myPage) ;
//    }
//
//    public void setOrdered(Ordered ordered) {
//        this.ordered = ordered ;
//        this.orderRejected = null ;
//    }
//
//    public void setOrderRejected(OrderRejected orderRejected) {
//        this.ordered = null ;
//        this.orderRejected = orderRejected ;
//    }
}