package com.study.mypizza.store.dto;

import com.study.mypizza.store.entity.StoreOrderDetail;
import lombok.*;

@Builder
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StoreOrderDetailDto {
    private Long orderDetailId;
    private Long orderId ;
    private Long itemId ;
    private String itemNm ;
    private Integer qty;
    private Integer pricePerOne;

    public void setItemNm(String itemNm) {
        this.itemNm = itemNm ;
    }

    public StoreOrderDetail toEntity() {
        return StoreOrderDetail.builder()
                .orderDetailId(orderDetailId)
                .orderId(orderId)
                .itemId(itemId)
                .qty(qty)
                .pricePerOne(pricePerOne)
                .build();
    }

    public static StoreOrderDetailDto of(StoreOrderDetail storeOrderDetail) {
        return StoreOrderDetailDto.builder()
                .orderDetailId(storeOrderDetail.getOrderDetailId())
                .orderId(storeOrderDetail.getOrderId())
                .itemId(storeOrderDetail.getItemId())
                .qty(storeOrderDetail.getQty())
                .pricePerOne(storeOrderDetail.getPricePerOne())
                .build();
    }
}
