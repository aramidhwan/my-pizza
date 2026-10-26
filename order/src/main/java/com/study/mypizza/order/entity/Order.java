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

import java.util.List;


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
    // OrderDetail 엔티티에서 order_id @JoinColumn 하므로 반드시 지정.
    @Column(name="order_id")
    private Long orderId;
    @Column(nullable = false)
    private int customerNo;
    @Column(nullable = true)
    private Long storeId;
    @Column(nullable = true)
    private Long deliveryId;
    @Convert(converter = OrderStatusConverter.class) // 변환기 적용
    @Column(name = "status", columnDefinition = "VARCHAR(255)", nullable = false)
    private OrderStatus status;
    private String statusInfo;
    private String regionNm;
    @Column(nullable = false)
    private Integer totalPrice;
    // 양방향 연관관계 지정 (Cascade 및 orphanRemoval 설정을 통해 주문 생명주기에 맞춰 주문상세를 한 번에 관리)
    // OrderRepository.save() 저장 시 (Cascade.ALL 로 인해 OrderDetail도 자동 저장됨)
    // mappedBy 속성에는 자식(상대방) 엔티티 클래스 안에 선언되어 있는 '부모 엔티티를 참조하는 필드(변수) 이름'을 넣어야 합니다.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetailList;

    public void assignOrderDetail(List<OrderDetail> orderDetailList) {
        orderDetailList.forEach(orderDetail -> {
            orderDetail.setOrder(this);
        });
        this.orderDetailList = orderDetailList ;
    }

    public void assignStore(Long storeId) {
        this.storeId = storeId ;
    }

    public void assignDelivery(Long deliveryId) {
        this.deliveryId = deliveryId ;
    }

    public void appendStatusInfo(String msg) {
        this.statusInfo += "::" + msg ;
    }

    public boolean updateStatus(OrderStatus nextStatus) {
        OrderStatus currStatus = this.status ;

        // 상태 전이 가능 여부 체크
        boolean isSucceeded = OrderStatus.canTransitionTo(this.orderId, currStatus, nextStatus) ;
        if (!isSucceeded) {
            return false;
        }

        this.status = nextStatus ;
        setStatusInfo(nextStatus.getStatus());
        return true;
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
        // kafka 이벤트 발송은 OrderService로 이관
        if ( status == OrderStatus.ORDERED ) {
            // kafka 이벤트 발행을 OrderService.createOrder()로 이동함
//            Ordered ordered = new Ordered();
//            BeanUtils.copyProperties(this, ordered);
//            ordered.publishAfterCommit();
//            log.info("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), ordered.toString());
        } else if ( status == OrderStatus.ORDER_REJECTED ) {
//            OrderRejected orderRejected = new OrderRejected();
//            BeanUtils.copyProperties(this, orderRejected);
//            orderRejected.publishAfterCommit();
//            log.info("#### Published in [{}.{}()] : {}", this.getClass().getSimpleName(), new Object(){}.getClass().getEnclosingMethod().getName(), orderRejected.toString());
        } else {
            log.error("⚠️ 주문 상태 이상!! onPostPersist()에서 존재할 수 없는 상태! : {}", this.toString()); ;
        }
    }

    @PostUpdate
    private void onPostUpdate() {
        if (this.status == null) {
            log.error("⚠️ onPostUpdate 주문 상태 이상..!! orderId=[{}], status=null", this.orderId);
            return;
        }

        AbstractEvent event = null;
        switch (this.status) {
            case COOKED -> {
                // 모든 경우의 status 상태 변화 시 이벤트 발행
                event = new Cooked();
            }
            case ORDER_ACCEPTED -> event = new OrderAccepted();
            case ORDER_REJECTED -> event = new OrderRejected();
            case ORDER_CANCELLED -> event = new OrderCancelled();
            case DELIVERY_ACCEPTED -> event = new DeliveryAccepted();
            case DELIVERY_STARTED -> event = new DeliveryStarted();
            case DELIVERED -> event = new Delivered();
            default -> {
                log.error("⚠️ onPostUpdate 주문 상태 이상..!! orderId=[{}], status={}", this.orderId, this.status);
                return;
            }
        }

        BeanUtils.copyProperties(this, event);
        event.publishAfterCommit();

        log.info("👉 카프카 이벤트 발송..!! : {}", event);

    }
}
