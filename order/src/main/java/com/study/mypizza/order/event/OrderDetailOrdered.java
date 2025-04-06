package com.study.mypizza.order.event;

import com.study.mypizza.order.dto.ItemDto;
import com.study.mypizza.order.dto.OrderDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class OrderDetailOrdered extends AbstractEvent {
    private Long orderDetailId;
    private Long orderId ;
    private Long itemId ;
    private Integer pricePerOne;
    private Integer qty;
}
