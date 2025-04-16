package com.study.mypizza.mypage.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name="t_mypage_order_detail")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Slf4j
public class MyPageOrderDetail {
    @Id
    private Long orderDetailId;
    @NotNull
    private Long orderId ;
    @NotNull
    private Long itemId ;
    private Integer qty;
    private Integer pricePerOne;
}
