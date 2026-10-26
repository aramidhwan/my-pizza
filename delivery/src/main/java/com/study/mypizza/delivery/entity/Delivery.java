package com.study.mypizza.delivery.entity;

import com.study.mypizza.delivery.config.OrderStatusConverter;
import com.study.mypizza.delivery.enums.OrderStatus;
import com.study.mypizza.delivery.event.Delivered;
import com.study.mypizza.delivery.event.DeliveryAccepted;
import com.study.mypizza.delivery.event.DeliveryStarted;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name="t_delivery")
@SuperBuilder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
@Slf4j
@EntityListeners(AuditingEntityListener.class)
public class Delivery extends BaseEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long deliveryId;
    @Column(nullable = false, unique = true)
    private Long orderId;
    @Column(nullable = false)
    private Long storeId;
    @Convert(converter = OrderStatusConverter.class) // 변환기 적용
    @Column(name = "status", columnDefinition = "VARCHAR(255)", nullable = false)
    private OrderStatus status;
    private int ownerNo ;

    public boolean updateStatus(OrderStatus nextStatus) {
        OrderStatus currStatus = this.status ;
        if (currStatus == nextStatus) {
            log.info("👉 [INFO] 이미 반영된 상태입니다! orderId={}, 현재상태:{}, 처리상태:{}", this.orderId, currStatus, nextStatus);
            return true ;
        }

        boolean isSucceeded = OrderStatus.canTransitionTo(this.orderId, currStatus, nextStatus) ;
        if (!isSucceeded) {
            return false;
        }
        this.status = nextStatus ;
        return true;
    }

    @PostPersist
    public void onPostPersist(){
        DeliveryAccepted deliveryAccepted = new DeliveryAccepted() ;
        BeanUtils.copyProperties(this, deliveryAccepted);
        deliveryAccepted.publishAfterCommit();
        log.info("👉 카프카 이벤트 발송..!! : {}", deliveryAccepted);
    }

    @PostUpdate
    public void onPostUpdate() {
        if ( OrderStatus.DELIVERY_ACCEPTED == this.status ) {
            DeliveryAccepted deliveryAccepted = new DeliveryAccepted() ;
            BeanUtils.copyProperties(this, deliveryAccepted);
            deliveryAccepted.publishAfterCommit();
            log.error("👉 (이거 발생하면 안되는데??) 카프카 이벤트 발송..!! : {}", deliveryAccepted);

        } else if ( OrderStatus.DELIVERY_STARTED == this.status) {
            DeliveryStarted deliveryStarted = new DeliveryStarted() ;
            BeanUtils.copyProperties(this, deliveryStarted);
            deliveryStarted.publishAfterCommit();
            log.info("👉 카프카 이벤트 발송..!! : {}", deliveryStarted);

        } else if ( OrderStatus.DELIVERED == this.status) {
            Delivered delivered = new Delivered() ;
            BeanUtils.copyProperties(this, delivered);
            delivered.publishAfterCommit();
            log.info("👉 카프카 이벤트 발송..!! : {}", delivered);
        }
    }
}
