package com.study.mypizza.store.entity;

import com.study.mypizza.store.event.Cooked;
import com.study.mypizza.store.event.OrderAccepted;
import com.study.mypizza.store.event.OrderRejected;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Entity
@Table(name="store_order_table")
@Data
@Slf4j
public class StoreOrder {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long storeOrderId;
    private Long storeId;
    private Long orderId;
    private Date acceptDt;
    private String status;

    @PrePersist
    private void onPrePersist() {
        this.setAcceptDt(new Date()) ;
    }

    @PostPersist
    private void onPostPersist() {
        OrderAccepted orderAccepted = new OrderAccepted() ;
        BeanUtils.copyProperties(this, orderAccepted);
        orderAccepted.publishAfterCommit();
        log.debug("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), orderAccepted.toString());
    }

    @PostUpdate
    private void onPostUpdate() {
        if ("OrderRejected".equals(this.status)) {
            OrderRejected orderRejected = new OrderRejected();
            BeanUtils.copyProperties(this, orderRejected);
            orderRejected.publishAfterCommit();
            log.debug("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object() {}.getClass().getEnclosingMethod().getName(), orderRejected.toString());
        } else if ("Cooked".equals(this.status)) {
            Cooked cooked = new Cooked() ;
            BeanUtils.copyProperties(this, cooked);
            cooked.publishAfterCommit();
            log.debug("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), cooked.toString());
        }
    }
}
