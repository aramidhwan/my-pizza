package com.study.mypizza.order.event;

import com.study.mypizza.order.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class StatusUpdated extends AbstractEvent {
    private Long orderId;
    private Long storeId;
    private OrderStatus status;
    private String statusInfo;
}
