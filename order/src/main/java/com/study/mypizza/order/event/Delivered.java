package com.study.mypizza.order.event;

import com.study.mypizza.order.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class Delivered extends AbstractEvent {
    private Long deliveryId;
    private Long orderId;
    private OrderStatus status;

}
