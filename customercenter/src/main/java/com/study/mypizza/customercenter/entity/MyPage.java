package com.study.mypizza.customercenter.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.study.mypizza.customercenter.config.OrderStatusConverter;
import com.study.mypizza.customercenter.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="t_mypage")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
public class MyPage {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long myPageId;
    private Long orderId;
    private int customerNo;
    private Long storeId;
    @Column(name = "status", columnDefinition = "VARCHAR(255)")
    @Convert(converter = OrderStatusConverter.class) // 변환기 적용
    private OrderStatus status;
    private String statusInfo;
    private String regionNm;
    private Integer totalPrice;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createDt;

    public void updateStoreId(Long storeId) {
        this.storeId = storeId ;
    }

    public void updateStatus(OrderStatus status, String statusInfo) {
        this.status = status ;
        this.statusInfo = statusInfo ;
    }
}
