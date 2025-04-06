package com.study.mypizza.delivery.controller;

import com.study.mypizza.delivery.dto.DeliveryDto;
import com.study.mypizza.delivery.dto.ResponseDto;
import com.study.mypizza.delivery.entity.Delivery;
import com.study.mypizza.delivery.enums.OrderStatus;
import com.study.mypizza.delivery.event.StatusUpdated;
import com.study.mypizza.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {
    private final DeliveryService deliveryService ;

//    @GetMapping("/deliveries/deliveryId/{status}")
//    @ResponseBody
//    public Delivery deliveryAcceptedDeliveryId(@PathVariable("status") String status) {
//        return deliveryService.deliveryAcceptedDeliveryId(status);
//    }

    @GetMapping("/html/deliveryAdminMain")
    public String getDeliveryAdminHtml() {
        log.trace("### [/html/deliveryAdminMain] is called.");
        return "/html/deliveryAdminMain" ;
    }

    // StoreAdmin 메인화면 (내 상점에 할당된 주문내역 가져오기)
    @GetMapping("/api/getDeliveryAdmin")
    @Secured("ROLE_DELIVERY_ADMIN")
    public ResponseEntity<ResponseDto> getDeliveryAdmin(Authentication authentication) {
        log.trace("### [getDeliveryAdmin] is called.");
        // Authentication 에서 customerNo 가져오기
        int customerNo = (Integer) authentication.getDetails();
        if (customerNo == 0) {
            throw new RuntimeException("### 이런 경우가 있어???") ;
        }
        List<DeliveryDto> deliveryDtos = deliveryService.getDeliveryAdmin(customerNo) ;

        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
                .data(deliveryDtos)
                .build();

        return ResponseEntity.ok(responseDto) ;
    }

    // 주문 status 업데이트 (배달시작 처리)
    @PostMapping("/api/updateOrderStatus")
    @Secured("ROLE_DELIVERY_ADMIN")
    public ResponseEntity<ResponseDto> updateOrderStatus(@RequestBody StatusUpdated statusUpdated) {
        log.trace("### [updateOrderStatus] is called. ###");
        String msgStatus = null ;
        DeliveryDto deliveryDto = deliveryService.updateOrderStatus(statusUpdated);

        if (deliveryDto.getStatus() == OrderStatus.DELIVERY_STARTED) {
            msgStatus = "배달시작" ;
        } else if (deliveryDto.getStatus() == OrderStatus.DELIVERED) {
            msgStatus = "배달완료" ;
        }
        ResponseDto responseDto = ResponseDto.builder()
                .BIZ_SUCCESS(0)
                .msg("주문번호["+deliveryDto.getOrderId()+"] "+ msgStatus +" 처리되었습니다.")
                .data(deliveryDto)
                .build();

        return ResponseEntity.ok(responseDto) ;
    }

}
