package com.study.mypizza.mypage.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.study.mypizza.mypage.entity.MyPage;
import com.study.mypizza.mypage.enums.OrderStatus;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Slf4j
public class MyPageDto {

    private Long mypageId;
    private Long orderId;
    private int customerNo;
    private Long storeId;
    private String storeNm;
    private String regionNm;
//    private List<ItemDto> itemDtos;
    private List<MyPageOrderDetailDto> myPageOrderDetailDtos;
    private OrderStatus status;
    private String statusInfo;
    private Integer totalPrice;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createDt;

    public void setStoreNm(String storeNm) {
        this.storeNm = storeNm ;
    }
    public void setMyPageOrderDetailDtos(List<MyPageOrderDetailDto> myPageOrderDetailDtos) {
        this.myPageOrderDetailDtos = myPageOrderDetailDtos ;
    }

    public MyPage toEntity() {
        return MyPage.builder()
                .orderId(orderId)
                .customerNo(customerNo)
                .storeId(storeId)
                .status(status)
                .statusInfo(statusInfo)
                .regionNm(regionNm)
                .totalPrice(totalPrice)
                .createDt(createDt)
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
                .build();
    }
}
