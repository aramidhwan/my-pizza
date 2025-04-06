package com.study.mypizza.store.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.study.mypizza.store.entity.Store;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@ToString
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StoreDto {
    private Long storeId;
    private String storeNm;
    private String addr;
    private String regionNm;
    private Boolean openYN;
    private int ownerNo ;
    private Long orderCnt;
    private List<StoreOrderDto> storeOrderDtos;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createDt;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updateDt ;

    public void setOrderCnt(Long orderCnt) {
        this.orderCnt = orderCnt ;
    }

    public void setStoreOrderDtos(List<StoreOrderDto> storeOrderDtos) {
        this.storeOrderDtos = storeOrderDtos ;
    }

    public Store toEntity() {
        return Store.builder()
                .storeId(storeId)
                .storeNm(storeNm)
                .addr(addr)
                .regionNm(regionNm)
                .openYN(openYN)
                .ownerNo(ownerNo)
                .build();
    }

    public static StoreDto of(Store store) {
        return StoreDto.builder()
                .storeId(store.getStoreId())
                .storeNm(store.getStoreNm())
                .addr(store.getAddr())
                .regionNm(store.getRegionNm())
                .openYN(store.getOpenYN())
                .ownerNo(store.getOwnerNo())
                .createDt(store.getCreateDt())
                .updateDt(store.getUpdateDt())
                .build();
    }
}
