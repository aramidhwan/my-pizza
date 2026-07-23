package com.study.mypizza.mypage.entity;

import com.study.mypizza.mypage.config.OrderStatusConverter;
import com.study.mypizza.mypage.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name="t_mypage")
@SuperBuilder
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MyPage extends BaseEntity {
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

    public void updateStoreId(Long storeId) {
        this.storeId = storeId ;
    }

    public void updateStatus(OrderStatus status, String statusInfo) {
        this.status = status ;
        this.statusInfo = statusInfo ;
    }
}
