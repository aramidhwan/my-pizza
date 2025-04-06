package com.study.mypizza.store.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.study.mypizza.store.entity.StoreOrder;
import com.study.mypizza.store.enums.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@ToString
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StoreOrderDto {
    private Long storeOrderId;
    @Builder.Default
    private StoreDto storeDto = StoreDto.builder().build();
    private Long orderId;
    private OrderStatus status;
    private List<StoreOrderDetailDto> storeOrderDetailDtos ;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createDt;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updateDt ;

    public void setStoreOrderDetailDtos(List<StoreOrderDetailDto> storeOrderDetailDtos) {
        this.storeOrderDetailDtos = storeOrderDetailDtos ;
    }

    public StoreOrder toEntity() {
        return StoreOrder.builder()
                .storeOrderId(storeOrderId)
                .orderId(orderId)
                .status(status)
                .build();
    }

    public static StoreOrderDto of(StoreOrder storeOrder) {
        return StoreOrderDto.builder()
                .storeOrderId(storeOrder.getStoreOrderId())
                .storeDto(StoreDto.of(storeOrder.getStore()))
                .orderId(storeOrder.getOrderId())
                .status(storeOrder.getStatus())
                .createDt(storeOrder.getCreateDt())
                .updateDt(storeOrder.getUpdateDt())
                .build();
    }
}
