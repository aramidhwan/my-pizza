package com.study.mypizza.store.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name="t_store")
@SuperBuilder
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DynamicInsert  // 실제 저장되는 필드만 insert into values 구문 날라간 것을 확인
@EntityListeners(AuditingEntityListener.class)
public class Store extends BaseEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="store_id")
    private Long storeId;
    private String storeNm;
    private String addr;
    private String regionNm;
    @NotNull
    private Boolean openYN;
    @Builder.Default
    private int ownerNo = 0;

//    @OneToMany(mappedBy = "store")
//    private List<StoreOrder> storeOrders;
}
