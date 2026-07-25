package com.study.mypizza.order.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name="t_item")
@Builder
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Item {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
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

