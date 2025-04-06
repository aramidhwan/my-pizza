package com.study.mypizza.customercenter.eventHandler.workers;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Setter
@Slf4j
public class ModifyMyPageWorkerThread implements Runnable {
//
//    @Autowired
//    MyPageRepository myPageRepository;
//    private StatusUpdated statusUpdated;
//
    @Override
    public void run() {
//        modifyMyPage();
    }
//
//    private void modifyMyPage() {
//        log.debug("### [{}] event received..!! : {}", new Object(){}.getClass().getEnclosingMethod().getName(), statusUpdated.toString());
//
//        Optional<MyPage> optional = myPageRepository.findByOrderId(statusUpdated.getOrderId());
//        if (optional.isPresent()) {
//            MyPage myPage = optional.get() ;
//            myPage.setStatus(statusUpdated.getStatus());
//            myPageRepository.save(myPage);
//        }
//    }
}