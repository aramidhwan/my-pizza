package com.study.mypizza.order.dto;

import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
//    @Positive(message = "고객번호(customerNo)가 없습니다. 관리자에게 문의해 주세요.")
    private int customerNo;
    @NotEmpty(message = "지역구(regionNm)가 없습니다. 관리자에게 문의해 주세요.")
    private String regionNm;
    private Integer totalPrice;
    @NotNull(message = "주문 메뉴(items)가 비었습니다.")
    private List<ItemDto> items;
}
