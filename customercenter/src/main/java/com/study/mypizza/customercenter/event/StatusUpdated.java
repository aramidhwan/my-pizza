package com.study.mypizza.customercenter.event;

import com.study.mypizza.customercenter.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class StatusUpdated extends AbstractEvent {
    private Long orderId;
    private Long storeId;
    private OrderStatus status;
    private String statusInfo;
}
