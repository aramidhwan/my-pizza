package com.study.mypizza.order.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name="t_item")
@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Item {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    // OrderDetail 엔티티에서 item_id로 @JoinColumn 하므로 반드시 지정.
    @Column(name="item_id")
    private Long itemId;
    @Column(nullable = false)
    private String itemNm;
    @Column(nullable = false)
    private String itemGroup;
    @Column(nullable = false)
    private Integer pricePerOne;
    @CreatedDate
    private LocalDateTime registDt;
}

