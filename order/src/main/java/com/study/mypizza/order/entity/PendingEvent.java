package com.study.mypizza.order.entity;

import com.study.mypizza.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name="t_pending_event")
@SuperBuilder
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PendingEvent extends BaseEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long eventId;
    @Column(nullable = false)
    private String msaGb;
    @Column(nullable = false)
    private String eventType;
    @Column(nullable = false)
    private OrderStatus status;
    @Column(nullable = false)
    private Long orderId;
    @Column(nullable = false)
    private String payload;
    @Column(nullable = false)
    @Builder.Default // 👈👉 @SuperBuilder 때문에 추가해야 함 👉
    @Setter
    private boolean isCompleted = false;
}
