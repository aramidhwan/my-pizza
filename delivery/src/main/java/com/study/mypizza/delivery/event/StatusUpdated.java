package com.study.mypizza.delivery.event;

import com.study.mypizza.delivery.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper=false)
public class StatusUpdated extends AbstractEvent {
    private Long orderId;
    private Long storeId;
    private Long customerId;
    private String pizzaNm;
    private Integer qty;
    private OrderStatus status;
    private String statusInfo;
    private String regionNm;
    private Date orderDt;
}
