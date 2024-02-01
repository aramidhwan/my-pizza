package com.study.mypizza.delivery.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class DeliveryStarted extends AbstractEvent {
    private Long deliveryId;
    private Long orderId;
    private String status;

}
