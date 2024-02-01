package com.study.mypizza.order.entity;

import com.study.mypizza.order.OrderApplication;
import com.study.mypizza.order.event.OrderCancelled;
import com.study.mypizza.order.event.OrderRejected;
import com.study.mypizza.order.event.Ordered;
import com.study.mypizza.order.event.StatusUpdated;
import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Entity
@Table(name="Order_table")
@Data
@Slf4j
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long orderId;
    private Long customerId;
    private String pizzaNm;
    private Integer qty;
    private String status;
    private String statusInfo;
    private String regionNm;
    private Date orderDt;

    public void setStatus(String status) {
        if (this.statusInfo != null) {
            this.setStatusInfo(this.statusInfo + "::" + status);
        } else {
            this.setStatusInfo(status);
        }
        this.status = status ;
    }

    @PrePersist
    private void onPrePersist() {
        // Req/Res Calling
        boolean bResult = false;

        // mappings goes here
        try {
            bResult = OrderApplication.applicationContext.getBean(com.study.mypizza.order.external.StoreService.class).checkOpenYn(this.regionNm) ;
        } catch(Exception e) {
            e.printStackTrace();
        }

        // 주문 요청된 지역에 오픈중인 Store가 있을 경우
        if ( bResult ) {
            this.setStatus("Ordered") ;
        // 주문 요청된 지역에 오픈중인 Store가 없을 경우
        } else {
            this.setStatus("OrderRejected") ;
        }

        this.orderDt = new Date();
    }

    @PostPersist
    private void onPostPersist() {
        log.debug("### {} :: {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName());
        if ( "Ordered".equals(this.status)) {
            Ordered ordered = new Ordered() ;
            BeanUtils.copyProperties(this, ordered);
            ordered.publishAfterCommit();
            log.debug("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), ordered.toString());
        } else if ( "OrderRejected".equals(this.status)) {
            OrderRejected orderRejected = new OrderRejected();
            BeanUtils.copyProperties(this, orderRejected);
            orderRejected.publishAfterCommit();
            log.debug("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), orderRejected.toString());
        }

    }

    @PostUpdate
    private void onPostUpdate() {
        // 주문 취소 시
        if ( "OrderCancelled".equals(this.status)) {
            OrderCancelled orderCancelled = new OrderCancelled() ;
            BeanUtils.copyProperties(this, orderCancelled);
            orderCancelled.publishAfterCommit();
            log.debug("#### Published in= [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), orderCancelled.toString());
        }

        // 모든 status 상태 변화 시 이벤트 발행
        StatusUpdated statusUpdated = new StatusUpdated();
        BeanUtils.copyProperties(this, statusUpdated);
        statusUpdated.publishAfterCommit();
        log.debug("#### Published in= [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), statusUpdated.toString());
    }


}
