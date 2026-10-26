package com.study.mypizza.store.dto;

import com.study.mypizza.store.entity.StoreOrder;
import com.study.mypizza.store.enums.OrderStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
@ToString(callSuper = true) // 👈 extends BaseDto 상위 클래스 필드까지 출력
@NoArgsConstructor(force = true, access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class StoreOrderDto extends BaseDto {
    private Long storeOrderId;
//    @Builder.Default
//    private StoreDto storeDto = StoreDto.builder().build();
    private Long orderId;
    private OrderStatus status;
    @Setter
    private List<StoreOrderDetailDto> storeOrderDetailDtoList;
    private String forTest;

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
//                .storeDto(StoreDto.of(storeOrder.getStore()))
                .orderId(storeOrder.getOrderId())
                .status(storeOrder.getStatus())
                .createDt(storeOrder.getCreateDt())
                .updateDt(storeOrder.getUpdateDt())
                .build();
    }
}
