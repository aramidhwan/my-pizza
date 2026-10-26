package com.study.mypizza.order.dto;

import com.study.mypizza.order.entity.Order;
import com.study.mypizza.order.entity.OrderDetail;
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
@ToString(callSuper = true) // 👈 extends BaseDto 상위 클래스 필드까지 출력
@Slf4j
public class OrderDto extends BaseDto {
    private Long orderId;
    private int customerNo;
    private Long storeId;
    private Long deliveryId;
    private String regionNm;
    private OrderStatus status;
    private String statusInfo;
    private Integer totalPrice;

    private List<OrderDetailDto> orderDetailDtoList;

    public Order toEntity() {
        return Order.builder()
                .orderId(orderId)
                .customerNo(customerNo)
                .storeId(storeId)
                .deliveryId(deliveryId)
                .status(status)
                .statusInfo(statusInfo)
                .regionNm(regionNm)
                .totalPrice(totalPrice)
                .orderDetailList(orderDetailDtoList==null? null:orderDetailDtoList.stream()
                                                    .map(OrderDetailDto::toEntity)
                                                    .toList())
                .build();
    }

    public static OrderDto of(Order order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .customerNo(order.getCustomerNo())
                .storeId(order.getStoreId())
                .deliveryId(order.getDeliveryId())
                .status(order.getStatus())
                .statusInfo(order.getStatusInfo())
                .regionNm(order.getRegionNm())
                .totalPrice(order.getTotalPrice())
                .orderDetailDtoList(order.getOrderDetailList()==null? null:order.getOrderDetailList().stream()
                                                            .map(OrderDetailDto::of)
                                                            .toList())
                .createDt(order.getCreateDt())
                .updateDt(order.getUpdateDt())
                .build();
    }
}
