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
    private Long orderId;
    private Long storeId;
    @Convert(converter = OrderStatusConverter.class) // 변환기 적용
    @Column(name = "status", columnDefinition = "VARCHAR(255)")
    private OrderStatus status;
    private int ownerNo ;

    @PostPersist
    public void onPostPersist(){
        DeliveryAccepted deliveryAccepted = new DeliveryAccepted() ;
        BeanUtils.copyProperties(this, deliveryAccepted);
        deliveryAccepted.publishAfterCommit();
        log.trace("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), deliveryAccepted.toString());
    }

    @PostUpdate
    public void onPostUpdate() {
        if ( OrderStatus.DELIVERY_ACCEPTED == this.status ) {
            DeliveryAccepted deliveryAccepted = new DeliveryAccepted() ;
            BeanUtils.copyProperties(this, deliveryAccepted);
            deliveryAccepted.publishAfterCommit();
            log.trace("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), deliveryAccepted.toString());

        } else if ( OrderStatus.DELIVERY_STARTED == this.status) {
            DeliveryStarted deliveryStarted = new DeliveryStarted() ;
            BeanUtils.copyProperties(this, deliveryStarted);
            deliveryStarted.publishAfterCommit();
            log.trace("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), deliveryStarted.toString());

        } else if ( OrderStatus.DELIVERED == this.status) {
            Delivered delivered = new Delivered() ;
            BeanUtils.copyProperties(this, delivered);
            delivered.publishAfterCommit();
            log.trace("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), delivered.toString());
        }
    }

    public void setStatus(OrderStatus status) {
        this.status = status ;
    }
}
