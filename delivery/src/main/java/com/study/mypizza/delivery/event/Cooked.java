package com.study.mypizza.delivery.event;

import com.study.mypizza.delivery.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper=false)
public class Cooked extends AbstractEvent {
    private Long storeOrderId;
    private Long storeId;
    private Long orderId;
    private Date acceptDt;
    private OrderStatus status;
}
