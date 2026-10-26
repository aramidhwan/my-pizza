package com.study.mypizza.mypage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name="t_mypage_orderdetail")
@Builder
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Slf4j
public class MyPageOrderDetail {
    @Id
    private Long orderDetailId;
    // Order/OrderDetail 연관관계의 키 컬럼(order_id)을 지정하고 해당 컬럼으로 Order 객체를 채우기 위해 @JoinColumn 사용
    @JoinColumn(name = "order_id", referencedColumnName = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mypageOrderDetail_mypage_orderId"))
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // 실무에서는 지연 로딩(LAZY) 추천
    @Setter
    @ToString.Exclude // 👈 연관관계 필드는 toString() 생성 대상에서 제외!
    private MyPage mypage;
    @Column(nullable = false)
    private Long itemId ;
    @Column(nullable = false)
    private Integer qty;
    @Column(nullable = false)
    private Integer pricePerOne;
}
