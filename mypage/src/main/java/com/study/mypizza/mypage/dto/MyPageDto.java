package com.study.mypizza.mypage.dto;

import com.study.mypizza.mypage.entity.MyPage;
import com.study.mypizza.mypage.enums.OrderStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Slf4j
public class MyPageDto extends BaseDto {

    private Long mypageId;
    private Long orderId;
    private int customerNo;
    private String customerName;
    private Long storeId;
    private Long deliveryId;
    @Setter
    private String storeNm;
    private String regionNm;
    private OrderStatus status;
    private String statusInfo;
    private Integer totalPrice;
    @Setter
    private List<MyPageOrderDetailDto> myPageOrderDetailDtoList;

    public MyPage toEntity() {
        return MyPage.builder()
                .orderId(orderId)
                .customerNo(customerNo)
                .storeId(storeId)
                .status(status)
                .statusInfo(statusInfo)
                .regionNm(regionNm)
                .totalPrice(totalPrice)
                .build();
    }

    public static MyPageDto of(MyPage myPage) {
        return MyPageDto.builder()
                .orderId(myPage.getOrderId())
                .customerNo(myPage.getCustomerNo())
                .storeId(myPage.getStoreId())
                .status(myPage.getStatus())
                .statusInfo(myPage.getStatusInfo())
                .regionNm(myPage.getRegionNm())
                .totalPrice(myPage.getTotalPrice())
                .createDt(myPage.getCreateDt())
                .updateDt(myPage.getUpdateDt())
                .build();
    }
}
