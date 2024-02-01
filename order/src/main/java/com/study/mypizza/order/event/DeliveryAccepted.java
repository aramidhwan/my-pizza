package com.study.mypizza.order.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class DeliveryAccepted extends AbstractEvent {
    private Long deliveryId;
    private Long orderId;
    private String status;
}
