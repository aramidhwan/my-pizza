package com.study.mypizza.customercenter.event;

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
    private String status;
    private String statusInfo;
    private String regionNm;
    private Date orderDt;

}
