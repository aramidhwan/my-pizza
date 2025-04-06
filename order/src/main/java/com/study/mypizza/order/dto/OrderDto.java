package com.study.mypizza.order.dto;

import com.study.mypizza.order.entity.Order;
import com.study.mypizza.order.enums.OrderStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Slf4j
public class OrderDto extends BaseDto {

    private Long orderId;
    private int customerNo;
    private Long storeId;
    private String regionNm;
    private List<ItemDto> itemDtos;
    private OrderStatus status;
    private String statusInfo;
    private Integer totalPrice;

    public Order toEntity() {
        return Order.builder()
                .orderId(orderId)
                .customerNo(customerNo)
                .storeId(storeId)
                .status(status)
                .statusInfo(statusInfo)
                .regionNm(regionNm)
                .totalPrice(totalPrice)
                .build();
    }

    public static OrderDto of(Order order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .customerNo(order.getCustomerNo())
                .storeId(order.getStoreId())
                .status(order.getStatus())
                .statusInfo(order.getStatusInfo())
                .regionNm(order.getRegionNm())
                .totalPrice(order.getTotalPrice())
                .createDt(order.getCreateDt())
                .updateDt(order.getUpdateDt())
                .build();
    }
}
