package com.study.mypizza.store.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name="t_store_orderdetail")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@EntityListeners(AuditingEntityListener.class)
public class StoreOrderDetail {
    @Id
//    @GeneratedValue(strategy=GenerationType.IDENTITY)
//    @Column(name="order_detail_id")
    private Long orderDetailId;
    // t_storeOrder/t_storeOrderDetail 연관관계의 키 컬럼(order_id)을 지정하고 해당 컬럼으로 Order 객체를 채우기 위해 @JoinColumn 사용
    // name : 내(t_orderDetail) 컬럼명, referencedColumnName : 상대방(t_order) 컬럼명
    @JoinColumn(name = "store_order_id", referencedColumnName = "store_order_id", foreignKey = @ForeignKey(name = "fk_storeOrderDetail_storeOrder"))
    // FetchType.LAZY : N+1 문제를 방지
    @ManyToOne(fetch = FetchType.LAZY)
    @Builder.Default
    @Setter
    @ToString.Exclude // 👈 연관관계 필드는 toString() 생성 대상에서 제외!
    private StoreOrder storeOrder = new StoreOrder();
//    private Long orderId ;
    private Long itemId ;
    private Integer qty;
    private Integer pricePerOne;
}
