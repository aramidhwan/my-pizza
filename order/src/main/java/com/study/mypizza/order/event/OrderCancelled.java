package com.study.mypizza.order.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper=false)
public class OrderCancelled extends AbstractEvent {
    private Long orderId;
    private Long customerId;
    private String pizzaNm;
    private Integer qty;
    private String status;
    private String regionNm;
    private Date orderDt;
}
