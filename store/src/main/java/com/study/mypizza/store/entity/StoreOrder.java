package com.study.mypizza.store.entity;

import com.study.mypizza.store.config.OrderStatusConverter;
import com.study.mypizza.store.enums.OrderStatus;
import com.study.mypizza.store.event.Cooked;
import com.study.mypizza.store.event.OrderAccepted;
import com.study.mypizza.store.event.OrderRejected;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name="t_store_order")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // JSON 변환 시 불필요한 필드 제거
@Slf4j
@EntityListeners(AuditingEntityListener.class)
public class StoreOrder extends BaseEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long storeOrderId;
    @ManyToOne
    @JoinColumn(name = "store_id", foreignKey = @ForeignKey(name = "fk_store_order_store_id"))
    @Builder.Default
//    @JsonIgnoreProperties("storeOrders") // 순환 참조 방지
    private Store store = new Store();
//    private Long storeId;
    private Long orderId;
    @Column(name = "status", columnDefinition = "VARCHAR(255)")
    @Convert(converter = OrderStatusConverter.class) // 변환기 적용
    private OrderStatus status;

    public void setStatus(OrderStatus status) {
        this.status = status ;
    }

    @PostPersist
    private void onPostPersist() {
        OrderAccepted orderAccepted = new OrderAccepted() ;
        BeanUtils.copyProperties(this, orderAccepted);
        orderAccepted.setStoreId(this.getStore().getStoreId());
        orderAccepted.publishAfterCommit();
        log.debug("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), orderAccepted.toString());
    }

    @PostUpdate
    private void onPostUpdate() {
        if (OrderStatus.ORDER_REJECTED == this.status) {
            OrderRejected orderRejected = new OrderRejected();
            BeanUtils.copyProperties(this, orderRejected);
            orderRejected.publishAfterCommit();
            log.debug("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object() {}.getClass().getEnclosingMethod().getName(), orderRejected.toString());
        // 조리완료 처리
        } else if (OrderStatus.COOKED == this.status) {
            Cooked cooked = new Cooked() ;
            BeanUtils.copyProperties(this, cooked);
            cooked.setStoreId(this.store.getStoreId());
            cooked.publishAfterCommit();
            log.debug("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), cooked.toString());
        }
    }
}
