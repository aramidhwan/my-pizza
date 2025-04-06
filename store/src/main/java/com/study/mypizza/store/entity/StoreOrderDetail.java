package com.study.mypizza.store.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name="t_store_order_detail")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Slf4j
@EntityListeners(AuditingEntityListener.class)
public class StoreOrderDetail {
    @Id
    private Long orderDetailId;
    // Order/OrderDetail 연관관계의 키 컬럼(order_id)을 지정하고 해당 컬럼으로 Order 객체를 채우기 위해 @JoinColumn 사용
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(name = "fk_storeorderdetail_storeorder_order_id"))
//    @Builder.Default
//    private StoreOrder storeOrder = new StoreOrder();
    private Long orderId ;
    private Long itemId ;
    private Integer qty;
    private Integer pricePerOne;
}
