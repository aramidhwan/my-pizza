package com.study.mypizza.order.controller;

import com.study.mypizza.order.dto.*;
import com.study.mypizza.order.exception.MyPizzaException;
import com.study.mypizza.order.service.ItemService;
import com.study.mypizza.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService ;
    private final ItemService itemService ;

    @GetMapping("/html/orderMain")
    public String orderMain(Model model) {
        List<ItemDto> itemDtos = itemService.getItems() ;
        model.addAttribute("itemDtos", itemDtos);
        model.addAttribute("regionNm", "강남구");
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        return "html/orderMain";
    }

    @PostMapping("/api/createOrder")
    @PreAuthorize("hasRole('ROLE_CUSTOMER') or hasRole('ROLE_ADMIN')") // 여러개 권한 주기
    public ResponseEntity<ResponseDto> createOrder(@RequestBody OrderRequestDto orderRequestDto, Authentication authentication) {
        Assert.isTrue(!orderRequestDto.getItems().isEmpty(), "한개라도 주문하라고!!");

        // Authentication 에서 customerNo 가져오기
        Integer customerNo = (Integer) authentication.getDetails();
        orderRequestDto.setCustomerNo(customerNo);
        OrderDto newOrderDto = orderService.createOrder(orderRequestDto) ;

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
                .msg("✅ 주문이 완료되었습니다!")
                .data(newOrderDto)
                .build();
        return ResponseEntity.ok(responseDto) ;
    }

    @PatchMapping("/orders/orderCancel/{orderId}")
    public String orderCancel(@PathVariable("orderId") Long orderId) throws MyPizzaException {
        orderService.orderCancel(orderId);
        return "취소되었습니다." ;
    }

    @GetMapping("/favicon.ico")
    public void returnNoFavicon() {
    }
}
