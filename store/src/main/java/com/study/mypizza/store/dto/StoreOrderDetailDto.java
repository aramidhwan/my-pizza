package com.study.mypizza.store.dto;

import com.study.mypizza.store.entity.StoreOrderDetail;
import lombok.*;

@Builder
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class StoreOrderDetailDto {
    private Long orderDetailId;
    private Long itemId ;
    @Setter
    private String itemNm ;
    private Integer qty;
    private Integer pricePerOne;

    public StoreOrderDetail toEntity() {
        return StoreOrderDetail.builder()
                .orderDetailId(orderDetailId)
                .itemId(itemId)
                .qty(qty)
                .pricePerOne(pricePerOne)
                .build();
    }

    public static StoreOrderDetailDto of(StoreOrderDetail storeOrderDetail) {
        return StoreOrderDetailDto.builder()
                .orderDetailId(storeOrderDetail.getOrderDetailId())
                .itemId(storeOrderDetail.getItemId())
                .qty(storeOrderDetail.getQty())
                .pricePerOne(storeOrderDetail.getPricePerOne())
                .build();
    }
}
