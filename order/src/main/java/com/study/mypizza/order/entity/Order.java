package com.study.mypizza.order.entity;

import com.study.mypizza.order.config.OrderStatusConverter;
import com.study.mypizza.order.event.*;
import com.study.mypizza.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name="t_order")
@SuperBuilder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
@Slf4j
@EntityListeners(AuditingEntityListener.class)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="order_id")
    private Long orderId;
    private int customerNo;
    private Long storeId;
    @Convert(converter = OrderStatusConverter.class) // 변환기 적용
    @Column(name = "status", columnDefinition = "VARCHAR(255)")
    private OrderStatus status;
    private String statusInfo;
    private String regionNm;
    private Integer totalPrice;

    public void assignStore(Long storeId) {
        this.storeId = storeId ;
    }

    public void statusUpdate(OrderStatus status) {
        this.status = status ;
        setStatusInfo(status.getStatus());
    }

    private void setStatusInfo(String statusInfo) {
        if ( statusInfo != null ) {
            if (this.statusInfo != null) {
                this.statusInfo = this.statusInfo + "::" + statusInfo;
            } else {
                this.statusInfo = statusInfo;
            }
        }
    }

    @PostPersist
    private void onPostPersist() {
//        log.debug("### {} :: {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName());
        if ( status == OrderStatus.ORDERED ) {
            Ordered ordered = new Ordered();
            BeanUtils.copyProperties(this, ordered);
            ordered.publishAfterCommit();
            log.trace("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), ordered.toString());
        } else if ( status == OrderStatus.ORDER_REJECTED ) {
            OrderRejected orderRejected = new OrderRejected();
            BeanUtils.copyProperties(this, orderRejected);
            orderRejected.publishAfterCommit();
            log.trace("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), orderRejected.toString());
        } else {
            log.warn("### SOMETHING WRONG!! onPostPersist : {}", this.toString()); ;
        }
    }

    @PostUpdate
    private void onPostUpdate() {
        if ( "Cooked".equals(this.status.getStatus())) {
            if ( "Ordered::Cooked".equals(this.statusInfo)) {
                log.debug("#### onPostUpdate 상태이상!!, Skip..!! : orderId={}, status={}[{}.{}()] : {}", orderId, status, this.getClass().getSimpleName(), new Object() {
                }.getClass().getEnclosingMethod().getName(), this.toString());
                return;
            }
            // 모든 경우의 status 상태 변화 시 이벤트 발행
            StatusUpdated statusUpdated = new StatusUpdated();
            BeanUtils.copyProperties(this, statusUpdated);
            statusUpdated.publishAfterCommit();
            log.trace("#### Published in= [{}.{}()] : {}", this.getClass().getSimpleName(), new Object() {
            }.getClass().getEnclosingMethod().getName(), statusUpdated.toString());

        } else if ( "OrderAccepted".equals(this.status.getStatus())) {
            OrderAccepted orderAccepted = new OrderAccepted();
            BeanUtils.copyProperties(this, orderAccepted);
            orderAccepted.publishAfterCommit();
            log.trace("#### Published in= [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), orderAccepted.toString());

        } else {
            // 모든 경우의 status 상태 변화 시 이벤트 발행
            StatusUpdated statusUpdated = new StatusUpdated();
            BeanUtils.copyProperties(this, statusUpdated);
            statusUpdated.publishAfterCommit();
            log.trace("#### Published in= [{}.{}()] : {}", this.getClass().getSimpleName(), new Object() {
            }.getClass().getEnclosingMethod().getName(), statusUpdated.toString());
        }
    }
}
