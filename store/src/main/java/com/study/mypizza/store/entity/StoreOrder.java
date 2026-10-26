package com.study.mypizza.store.entity;

import com.study.mypizza.store.config.OrderStatusConverter;
import com.study.mypizza.store.dto.OrderDetailDto;
import com.study.mypizza.store.enums.OrderStatus;
import com.study.mypizza.store.event.OrderAccepted;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="t_store_order")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // JSON 변환 시 불필요한 필드 제거
@Slf4j
@EntityListeners(AuditingEntityListener.class)
public class StoreOrder extends BaseEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="store_order_id")
    private Long storeOrderId;
    @ManyToOne
    @JoinColumn(name = "store_id", foreignKey = @ForeignKey(name = "fk_store_order_store_id"))
    private Store store ;
//    private Long storeId;
    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;
    @Column(name = "status", columnDefinition = "VARCHAR(255)")
    @Convert(converter = OrderStatusConverter.class) // 변환기 적용
    @Setter
    private OrderStatus status;
    // 양방향 연관관계 지정 (Cascade 및 orphanRemoval 설정을 통해 주문 생명주기에 맞춰 주문상세를 한 번에 관리)
    // OrderRepository.save() 저장 시 (Cascade.ALL 로 인해 OrderDetail도 자동 저장됨)
    // mappedBy 속성에는 자식(상대방) 엔티티 클래스 안에 선언되어 있는 '부모 엔티티를 참조하는 필드(변수) 이름'을 넣어야 합니다.
    @OneToMany(mappedBy = "storeOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreOrderDetail> storeOrderDetailList;

    // 주문상세(OrderDetail) 셋팅
    public void assignStoreOrderDetail(List<OrderDetailDto> orderDetailList) {
        this.storeOrderDetailList = new ArrayList<>() ;
        for (OrderDetailDto orderDetailDto : orderDetailList) {
            this.storeOrderDetailList.add(StoreOrderDetail.builder()
                            .orderDetailId(orderDetailDto.getOrderDetailId())
                            .storeOrder(this)
                            .itemId(orderDetailDto.getItemDto().getItemId())
                            .qty(orderDetailDto.getQty())
                            .pricePerOne(orderDetailDto.getPricePerOne())
                            .build()) ;
        }
    }

    // 본 메소드는 StoreService.updateOrderStatus() 에서 호출되어야 함
    public boolean updateStatus(OrderStatus nextStatus) {
        OrderStatus currStatus = this.status ;
        // 상태 전이 가능 여부 체크
        boolean canTransition = OrderStatus.canTransitionTo(this.orderId, currStatus, nextStatus) ;
        if (!canTransition) {
            return false;
        }

        this.status = nextStatus ;
        return true;
    }

    @PostPersist
    private void onPostPersist() {
        if (OrderStatus.ORDER_ACCEPTED == this.status) {
            OrderAccepted orderAccepted = new OrderAccepted() ;
            BeanUtils.copyProperties(this, orderAccepted);
            orderAccepted.setStoreId(this.getStore().getStoreId());
            orderAccepted.publishAfterCommit();
            log.info("👉 카프카 이벤트 발송..!! : {}", orderAccepted);
        }
    }

    @PostUpdate
    private void onPostUpdate() {
        // 아래 기능은 StoreService로 이동
//        if (OrderStatus.ORDER_ACCEPTED == this.status) {
//            OrderAccepted orderAccepted = new OrderAccepted() ;
//            BeanUtils.copyProperties(this, orderAccepted);
//            orderAccepted.setStoreId(this.getStore().getStoreId());
//            orderAccepted.publishAfterCommit();
//            log.info("👉 카프카 이벤트 발송..!! : {}", orderAccepted);
//        } else if (OrderStatus.ORDER_REJECTED == this.status) {
//            OrderRejected orderRejected = new OrderRejected();
//            BeanUtils.copyProperties(this, orderRejected);
//            orderRejected.publishAfterCommit();
//            log.info("👉 카프카 이벤트 발송..!! : {}", orderRejected);
//            // 조리완료 처리
//        } else if (OrderStatus.COOKED == this.status) {
//            Cooked cooked = new Cooked() ;
//            BeanUtils.copyProperties(this, cooked);
//            cooked.setStoreId(this.store.getStoreId());
//            cooked.publishAfterCommit();
//            log.info("👉 카프카 이벤트 발송..!! : {}", cooked);
//            // 접수취소 처리
//        } else if (OrderStatus.ORDER_CANCELLED == this.status) {
//            OrderCancelled orderCancelled = new OrderCancelled() ;
//            BeanUtils.copyProperties(this, orderCancelled);
//            orderCancelled.publishAfterCommit();
//            log.info("👉 카프카 이벤트 발송..!! : {}", orderCancelled);
//        }
    }
}
