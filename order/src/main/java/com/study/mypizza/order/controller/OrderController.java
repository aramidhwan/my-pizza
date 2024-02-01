package com.study.mypizza.order.controller;

import com.study.mypizza.order.entity.Order;
import com.study.mypizza.order.external.StoreService;
import com.study.mypizza.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Slf4j
public class OrderController {
    @Autowired
    OrderRepository orderRepository ;
    @Autowired
    StoreService storeService ;

    @GetMapping("/orderCancel/{orderId}")
    public String orderCancel(@PathVariable("orderId") Long orderId) {
        String rtnMsg = null ;
        Optional<Order> optional = orderRepository.findById(orderId) ;

        if (optional.isPresent()) {
            Order order = optional.get() ;

            // 주문 접수 이전 단계에서만 취소가능
            if ( "Ordered".equals(order.getStatus()) ) {
                // 취소 전에 주문받은 Store가 있는지 Store MSA에서 다시 한 번 체크
                if (storeService.checkOrderCancel(orderId)) {
                    order.setStatus("OrderCancelled");
                    orderRepository.save(order) ;
                // 취소 불가능
                } else {
                    rtnMsg = "### 주문 취소 불가능 :: 이미 Store에서 준비중입니다." ;
                }
            } else {
                // 취소 불가능
                rtnMsg = "### 주문 취소 불가능 :: 이미 Ordered 이후 단계("+order.getStatus()+")로 진행되었습니다." ;
            }
        }

        log.debug(rtnMsg);
        return rtnMsg ;
    }
}
