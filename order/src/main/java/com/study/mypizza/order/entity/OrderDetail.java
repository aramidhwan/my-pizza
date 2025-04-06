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
    // Order/OrderDetail 연관관계의 키 컬럼(order_id)을 지정하고 해당 컬럼으로 Order 객체를 채우기 위해 @JoinColumn 사용
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(name = "fk_orderdetail_order_id"))
    @ManyToOne
    @Builder.Default
    private Order order = new Order();
    @ManyToOne
    @JoinColumn(name = "item_id", foreignKey = @ForeignKey(name = "fk_orderdetail_item_id"))
    @Builder.Default
    private Item item = new Item();
    private Integer qty;
    private Integer pricePerOne;

    @PostPersist
    private void onPostPersist() {
        OrderDetailOrdered orderDetailOrdered = new OrderDetailOrdered();
        BeanUtils.copyProperties(this, orderDetailOrdered);
        orderDetailOrdered.setOrderId(this.order.getOrderId());
        orderDetailOrdered.setItemId(this.item.getItemId());
        orderDetailOrdered.publishAfterCommit();
        log.trace("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), orderDetailOrdered.toString());
    }
}
