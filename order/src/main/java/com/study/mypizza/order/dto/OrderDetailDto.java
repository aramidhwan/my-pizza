package com.study.mypizza.order.dto;

import com.study.mypizza.order.entity.OrderDetail;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Builder
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class OrderDetailDto {
    private Long orderDetailId;
    @Builder.Default
    private OrderDto orderDto = new OrderDto() ;
    @Builder.Default
    private ItemDto itemDto = new ItemDto();
    private Integer qty;
    private Integer pricePerOne;

    public OrderDetail toEntity() {
        return OrderDetail.builder()
                .orderDetailId(orderDetailId)
                .order(orderDto.toEntity())
                .item(itemDto.toEntity())
                .qty(qty)
                .pricePerOne(pricePerOne)
                .build();
    }

    public static OrderDetailDto of(OrderDetail orderDetail) {
        return OrderDetailDto.builder()
                .orderDetailId(orderDetail.getOrderDetailId())
                .orderDto(OrderDto.of(orderDetail.getOrder()))
                .itemDto(ItemDto.of(orderDetail.getItem()))
                .qty(orderDetail.getQty())
                .pricePerOne(orderDetail.getPricePerOne())
                .build();
    }
}
