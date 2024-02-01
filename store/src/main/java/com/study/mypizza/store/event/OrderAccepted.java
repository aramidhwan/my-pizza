package com.study.mypizza.store.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper=false)
public class OrderAccepted extends AbstractEvent {
    private Long storeOrderId;
    private Long storeId;
    private Long orderId;
    private Date acceptDt;
    private String status;
}
