package com.study.mypizza.order.entity;

import com.study.mypizza.order.event.OrderDetailOrdered;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="t_orderdetail")
@Builder
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Slf4j
public class OrderDetail {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long orderDetailId;
    // t_order/t_orderDetail 연관관계의 키 컬럼(order_id)을 지정하고 해당 컬럼으로 Order 객체를 채우기 위해 @JoinColumn 사용
    // name : 내(t_orderDetail) 컬럼명, referencedColumnName : 상대방(t_order) 컬럼명
    @JoinColumn(name = "order_id", referencedColumnName = "order_id", foreignKey = @ForeignKey(name = "fk_orderdetail_order"))
    // FetchType.LAZY : N+1 문제를 방지
    @ManyToOne(fetch = FetchType.LAZY)
    @Builder.Default
    @Setter
    @ToString.Exclude // 👈 연관관계 필드는 toString() 생성 대상에서 제외! (lazy loading field는 toString() 제외 추천)
    private Order order = new Order();
    // t_order/t_orderDetail 연관관계의 키 컬럼(order_id)을 지정하고 해당 컬럼으로 Order 객체를 채우기 위해 @JoinColumn 사용
    // name : 내(t_orderDetail) 컬럼명, referencedColumnName : 상대방(t_item) 컬럼명
    @JoinColumn(name = "item_id", referencedColumnName = "item_id", foreignKey = @ForeignKey(name = "fk_orderdetail_item"))
    // FetchType.LAZY : N+1 문제를 방지
    @ManyToOne(fetch = FetchType.LAZY)
    @Builder.Default
    private Item item = new Item();
    @Column(nullable = false)
    private Integer qty;
    // 주문 당시의 단가 별도 저장
    @Column(nullable = false)
    private Integer pricePerOne;

    @PostPersist
    private void onPostPersist() {
        // 주문 시 2개 이벤트(Ordered, OrderDetailOrdered)가 발생하던 것을 1개 이벤트로 통합하기 위해
        // kafka 이벤트 발행을 OrderService.createOrder()로 이동함
//        OrderDetailOrdered orderDetailOrdered = new OrderDetailOrdered();
//        BeanUtils.copyProperties(this, orderDetailOrdered);
////        orderDetailOrdered.setOrderId(this.order.getOrderId());
//        orderDetailOrdered.setItemId(this.item.getItemId());
//        orderDetailOrdered.publishAfterCommit();
//        log.trace("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), orderDetailOrdered.toString());
    }
}
