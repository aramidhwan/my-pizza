package com.study.mypizza.store.event;

import com.study.mypizza.store.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class Ordered extends AbstractEvent {
    private Long orderId;
    private Long storeId;
    private Long customerId;
    private String pizzaNm;
    private Integer qty;
    private OrderStatus status;
    private String statusInfo;
    private String regionNm;
    private Integer totalPrice;
}
