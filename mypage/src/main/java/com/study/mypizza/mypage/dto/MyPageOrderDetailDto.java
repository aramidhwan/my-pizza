package com.study.mypizza.mypage.dto;

import com.study.mypizza.mypage.entity.MyPage;
import com.study.mypizza.mypage.entity.MyPageOrderDetail;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MyPageOrderDetailDto {
    private Long orderDetailId;
    @NotNull
    private Long itemId ;
    @Setter
    private String itemNm ;
    private Integer qty;
    private Integer pricePerOne;

    public MyPageOrderDetail toEntity(MyPage myPage) {
        return MyPageOrderDetail.builder()
                .orderDetailId(orderDetailId)
                .mypage(myPage)
                .itemId(itemId)
                .qty(qty)
                .pricePerOne(pricePerOne)
                .build();
    }

    public static MyPageOrderDetailDto of(MyPageOrderDetail myPageOrderDetail) {
        return MyPageOrderDetailDto.builder()
                .orderDetailId(myPageOrderDetail.getOrderDetailId())
                .itemId(myPageOrderDetail.getItemId())
                .qty(myPageOrderDetail.getQty())
                .pricePerOne(myPageOrderDetail.getPricePerOne())
                .build();
    }
}
