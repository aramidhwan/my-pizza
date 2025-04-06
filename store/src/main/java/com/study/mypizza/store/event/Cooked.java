package com.study.mypizza.store.event;

import com.study.mypizza.store.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class Cooked extends AbstractEvent {
    private Long storeOrderId;
    private Long storeId;
    private Long orderId;
    private OrderStatus status;
}
