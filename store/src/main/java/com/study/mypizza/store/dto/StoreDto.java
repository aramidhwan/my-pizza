package com.study.mypizza.store.dto;

import com.study.mypizza.store.entity.Store;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
@ToString
@NoArgsConstructor(force = true, access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class StoreDto extends BaseDto {
    private Long storeId;
    private String storeNm;
    private String addr;
    private String regionNm;
    private String orderAcceptMode;
    private Boolean openYN;
    private int ownerNo ;
    @Setter
    private Long orderCnt;
    @Setter
    private List<StoreOrderDto> storeOrderDtoList;

    public Store toEntity() {
        return Store.builder()
                .storeId(storeId)
                .storeNm(storeNm)
                .addr(addr)
                .regionNm(regionNm)
                .orderAcceptMode(orderAcceptMode)
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
                .orderAcceptMode(store.getOrderAcceptMode())
                .openYN(store.getOpenYN())
                .ownerNo(store.getOwnerNo())
                .build();
    }
}
