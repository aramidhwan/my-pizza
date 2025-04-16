package com.study.mypizza.mypage.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Builder
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ItemDto {
    private Long itemId;
    private String itemNm;
    private String itemGroup;
    private Integer pricePerOne;
    private Date registDt;
    private Integer qty;        // (Entity와 무관) 주문을 받기 위한 필드
}
