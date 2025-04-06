package com.study.mypizza.order.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Slf4j
public class OrderRequestDto {

//    private Long orderId;
    private int customerNo;
//    private Long storeId;
    private String regionNm;
    private Integer totalPrice;
    private List<ItemDto> items;
//    private List<ItemDto> itemDtos;
//    private String status;
//    private String statusInfo;
//    private Date orderDt;

//    public Order toEntity() {
//        return Order.builder()
//                .orderId(orderId)
//                .customerId(customerId)
//                .storeId(storeId)
//                .status(status)
//                .statusInfo(statusInfo)
//                .regionNm(regionNm)
//                .orderDt(orderDt)
//                .build();
//    }
//
//    public static OrderRequestDto of(Order order) {
//        return OrderRequestDto.builder()
//                .orderId(order.getOrderId())
//                .customerId(order.getCustomerId())
//                .storeId(order.getStoreId())
//                .status(order.getStatus())
//                .statusInfo(order.getStatus())
//                .regionNm(order.getRegionNm())
//                .orderDt(order.getOrderDt())
//                .build();
//    }
}
