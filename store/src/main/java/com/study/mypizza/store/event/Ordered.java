package com.study.mypizza.store.event;

import com.study.mypizza.store.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class Ordered extends AbstractEvent {
    private Long orderId;
    private Long storeId;
    private Long customerId;
    private String pizzaNm;
    private Integer qty;
    private OrderStatus status;
    private String statusInfo;
    private String regionNm;
    private Integer totalPrice;

    /**
     * 동일 aggregate의 이벤트 순서를 보장하기 위한 Kafka key.
     * 각 key는 AbstractEvent를 상속받는 이벤트에서 직접 구현한다.
     * getPartitionKey() 구현이 없는 이벤트는 AbstractEvent에서 null을 반환한다.
     */
    @Override
    protected String getPartitionKey() {
        return orderId == null ? null : orderId.toString();
    }
}
