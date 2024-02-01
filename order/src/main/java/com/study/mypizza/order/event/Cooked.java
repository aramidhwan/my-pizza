package com.study.mypizza.order.event;

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
    private String status;
}
