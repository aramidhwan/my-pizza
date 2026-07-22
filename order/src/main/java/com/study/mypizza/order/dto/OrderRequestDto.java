package com.study.mypizza.order.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Slf4j
public class OrderRequestDto {
    @Setter
    private int customerNo;
    private String regionNm;
    private Integer totalPrice;
    private List<ItemDto> items;
}
