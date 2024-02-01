package com.study.mypizza.order.eventHandler.workers;

import com.study.mypizza.order.OrderApplication;
import com.study.mypizza.order.entity.Order;
import com.study.mypizza.order.repository.OrderRepository;
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
        updateStatus();
    }

    // Store에서 주문접수/주문거절 시 상태 업데이트(OrderAccepted, OrderRejected)
    private void updateStatus() {
        Optional<Order> orderOptional = orderRepository.findById(this.orderId) ;
        if ( orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(this.status);
            orderRepository.save(order);
        }
    }
}