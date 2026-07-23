package com.study.mypizza.mypage.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.study.mypizza.mypage.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper=false)
public class OrderRejected extends AbstractEvent {
    private Long orderId;
    private Long storeId;
    private int customerNo;
    private Integer qty;
    private OrderStatus status;
    private String statusInfo;
    private String regionNm;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createDt;

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
