package com.study.mypizza.customercenter.dto;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StoreDto {
    private Long storeId;
    private String storeNm;
    private String addr;
    private String regionNm;
    private Boolean openYN;
}
