package com.study.mypizza.mypage.entity;

import com.study.mypizza.mypage.config.OrderStatusConverter;
import com.study.mypizza.mypage.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Entity
@Table(name="t_mypage")
@SuperBuilder
@Getter
@ToString
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MyPage extends BaseEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long myPageId;
    // MyPageOrderDetail에서 @JoinColumn으로 "order_id" 지정하므로 name = "order_id" 필수)
    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;
    @Column(nullable = false)
    private int customerNo;
    @Column(nullable = true)    /* open_yn 상정이 없으면 null 가능 */
    @Setter
    private Long storeId;
    @Column(nullable = true)    /* 배달접수 전까지 null 가능 */
    @Setter
    private Long deliveryId;
    @Column(name = "status", columnDefinition = "VARCHAR(255)", nullable = false)
    @Convert(converter = OrderStatusConverter.class) // 변환기 적용
    private OrderStatus status;
    private String statusInfo;
    @Column(nullable = false)
    private String regionNm;
    @Column(nullable = false)
    private Integer totalPrice;
    // 양방향 연관관계 지정 (Cascade 및 orphanRemoval 설정을 통해 주문 생명주기에 맞춰 주문상세를 한 번에 관리)
    // MyPageRepository.save() 저장 시 (Cascade.ALL 로 인해 MyPageDetail도 자동 저장됨)
    // mappedBy 속성에는 자식(상대방) 엔티티 클래스 안에 선언되어 있는 '부모 엔티티를 참조하는 필드(변수) 이름'을 넣어야 합니다.
    @OneToMany(mappedBy = "mypage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MyPageOrderDetail> myPageOrderDetailList;

    public void assignMyPageOrderDetail(List<MyPageOrderDetail> myPageOrderDetailList) {
        myPageOrderDetailList.forEach(myPageOrderDetail -> {
            myPageOrderDetail.setMypage(this);
        });
        this.myPageOrderDetailList = myPageOrderDetailList ;
    }

    public boolean updateStatus(OrderStatus nextStatus, String statusInfo) {
        OrderStatus currStatus = this.status ;
        if (currStatus == nextStatus) {
            log.info("👉 이미 반영된 상태입니다! orderId=[{}], 현재상태:{}, 처리상태:{}", this.orderId, currStatus, nextStatus);
            return true ;
        }

        // 상태 전이 가능 여부 체크
        boolean isSucceeded = OrderStatus.canTransitionTo(this.orderId, currStatus, nextStatus) ;
        if (!isSucceeded) {
            return false;
        }
        this.status = nextStatus ;
        if ( statusInfo != null && !statusInfo.isEmpty()) {
            this.statusInfo = statusInfo;
        } else {
            this.statusInfo = this.statusInfo + "::" + nextStatus.getStatus() ;
        }
        return true;
    }
}
