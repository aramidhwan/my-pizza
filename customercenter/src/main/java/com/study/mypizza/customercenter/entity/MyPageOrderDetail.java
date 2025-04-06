package com.study.mypizza.customercenter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
