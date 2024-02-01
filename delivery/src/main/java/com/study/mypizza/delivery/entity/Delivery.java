package com.study.mypizza.delivery.entity;

import com.study.mypizza.delivery.event.Delivered;
import com.study.mypizza.delivery.event.DeliveryAccepted;
import com.study.mypizza.delivery.event.DeliveryStarted;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Delivery_table")
@Data
@Slf4j
public class Delivery {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long deliveryId;
    private Long orderId;
    private String status;

    @PostPersist
    public void onPostPersist(){
        DeliveryAccepted deliveryAccepted = new DeliveryAccepted() ;
        BeanUtils.copyProperties(this, deliveryAccepted);
        deliveryAccepted.publishAfterCommit();
        log.debug("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), deliveryAccepted.toString());
    }

    @PostUpdate
    public void onPostUpdate() {
        if ( "DeliveryStarted".equals(this.status)) {
            DeliveryStarted deliveryStarted = new DeliveryStarted() ;
            BeanUtils.copyProperties(this, deliveryStarted);
            deliveryStarted.publishAfterCommit();
            log.debug("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), deliveryStarted.toString());

        } else if ( "Delivered".equals(this.status)) {
            Delivered delivered = new Delivered() ;
            BeanUtils.copyProperties(this, delivered);
            delivered.publishAfterCommit();
            log.debug("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), delivered.toString());
        }
    }
}
