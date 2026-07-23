package com.study.mypizza.store.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class OrderDetailOrdered extends AbstractEvent {
    private Long orderDetailId;
    private Long orderId ;
    private Long itemId ;
    private Integer pricePerOne;
    private Integer qty;

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
