package com.study.mypizza.order.eventHandler.workers;

import com.study.mypizza.order.entity.Order;
import com.study.mypizza.order.repository.OrderRepository;
import com.study.mypizza.order.enums.OrderStatus;
import jakarta.transaction.Transactional;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@Setter
public class UpdateStatusWorkerThread implements Runnable {
    @Autowired
    OrderRepository orderRepository;

    private Long orderId;
    private String status;

    @Override
    public void run() {
        doJob();
    }

    // Store에서 주문접수/주문거절 시 상태 업데이트(OrderAccepted, OrderRejected)
    @Transactional
    private void doJob() {
        log.debug("### [{}] event received..!1! : orderId={}, status={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, status);

        Optional<Order> orderOptional = orderRepository.findById(this.orderId) ;
        if ( orderOptional.isPresent()) {
            Order order = orderOptional.get();

            // 중복처리 방지
//            if ("OrderAccepted".equals(this.status) && !"Ordered".equals(order.getStatus())) {
//                log.debug("### [{}] event 상태이상!!, Skip..!! : orderId={}, status={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, status);
//                return ;
//            } else if ("Cooked".equals(this.status) && !"OrderAccepted".equals(order.getStatus())) {
//                log.debug("### [{}] event 상태이상!!, Skip..!! : orderId={}, status={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, status);
//                return ;
//            } else if ("DeliveryAccepted".equals(this.status) && !"Cooked".equals(order.getStatus())) {
//                log.debug("### [{}] event 상태이상!!, Skip..!! : orderId={}, status={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, status);
//                return ;
//            } else if ("DeliveryStarted".equals(this.status) && !"DeliveryAccepted".equals(order.getStatus())) {
//                log.debug("### [{}] event 상태이상!!, Skip..!! : orderId={}, status={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, status);
//                return ;
//            } else if ("Delivered".equals(this.status) && !"DeliveryStarted".equals(order.getStatus())) {
//                log.debug("### [{}] event 상태이상!!, Skip..!! : orderId={}, status={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, status);
//                return ;
//            }

            order.statusUpdate(OrderStatus.fromStatus(this.status));
            log.debug("### [{}] event received..!2! : orderId={}, status={}", new Object(){}.getClass().getEnclosingMethod().getName(), orderId, order.getStatus());
            orderRepository.save(order);
        }
    }
}