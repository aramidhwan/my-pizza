package com.study.mypizza.order.event;

import com.study.mypizza.order.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper=false)
public class OrderCancelled extends AbstractEvent {
    private Long orderId;
    private Long customerNo;
    private String pizzaNm;
    private Integer qty;
    private OrderStatus status;
    private String regionNm;
    private Date orderDt;
}
