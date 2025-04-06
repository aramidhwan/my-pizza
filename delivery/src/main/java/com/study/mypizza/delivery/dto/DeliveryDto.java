package com.study.mypizza.delivery.dto;

import com.study.mypizza.delivery.config.OrderStatusConverter;
import com.study.mypizza.delivery.entity.Delivery;
import com.study.mypizza.delivery.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;


@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Slf4j
public class DeliveryDto extends BaseDto {
    private Long deliveryId;
    private Long orderId;
    @Builder.Default
    private StoreDto storeDto = StoreDto.builder().build();
    private OrderStatus status;
    private int ownerNo ;

    public static DeliveryDto of(Delivery delivery) {
        return DeliveryDto.builder()
                .deliveryId(delivery.getDeliveryId())
                .orderId(delivery.getOrderId())
                .storeDto(StoreDto.builder().storeId(delivery.getStoreId()).build())
                .status(delivery.getStatus())
                .ownerNo(delivery.getOwnerNo())
                .createDt(delivery.getCreateDt())
                .updateDt(delivery.getUpdateDt())
                .build() ;
    }
}
