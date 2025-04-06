package com.study.mypizza.customercenter.dto;

import com.study.mypizza.customercenter.entity.MyPageOrderDetail;
import lombok.*;

@Builder
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MyPageOrderDetailDto {
    private Long orderDetailId;
    private Long orderId ;
    private Long itemId ;
    private String itemNm ;
    private Integer qty;
    private Integer pricePerOne;

    public void setItemNm(String itemNm) {
        this.itemNm = itemNm ;
    }

    public MyPageOrderDetail toEntity() {
        return MyPageOrderDetail.builder()
                .orderDetailId(orderDetailId)
                .orderId(orderId)
                .itemId(itemId)
                .qty(qty)
                .pricePerOne(pricePerOne)
                .build();
    }

    public static MyPageOrderDetailDto of(MyPageOrderDetail storeOrderDetail) {
        return MyPageOrderDetailDto.builder()
                .orderDetailId(storeOrderDetail.getOrderDetailId())
                .orderId(storeOrderDetail.getOrderId())
                .itemId(storeOrderDetail.getItemId())
                .qty(storeOrderDetail.getQty())
                .pricePerOne(storeOrderDetail.getPricePerOne())
                .build();
    }
}
