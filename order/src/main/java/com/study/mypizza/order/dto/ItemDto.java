package com.study.mypizza.order.dto;

import com.study.mypizza.order.entity.Item;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ItemDto {
    private Long itemId;
    private String itemNm;
    private String itemGroup;
    private Integer pricePerOne;
    private LocalDateTime registDt;
    private Integer qty;        // (Entity와 무관) 주문을 받기 위한 필드

    public Item toEntity() {
        return Item.builder()
                .itemId(itemId)
                .itemNm(itemNm)
                .itemGroup(itemGroup)
                .pricePerOne(pricePerOne)
                .registDt(registDt)
                .build();
    }

    public static ItemDto of(Item item) {
        return ItemDto.builder()
                .itemId(item.getItemId())
                .itemNm(item.getItemNm())
                .itemGroup(item.getItemGroup())
                .pricePerOne(item.getPricePerOne())
                .registDt(item.getRegistDt())
                .build();
    }
}
