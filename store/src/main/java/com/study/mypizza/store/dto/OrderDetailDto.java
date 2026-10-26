package com.study.mypizza.store.dto;

//import com.study.mypizza.mypage.entity.MyPageOrderDetail;
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
//    @Builder.Default
//    private OrderDto orderDto = new OrderDto() ;
    @Builder.Default
    private ItemDto itemDto = new ItemDto();
    private Integer qty;
    private Integer pricePerOne;

    // mypage MSA에만 존재하는 메소드임
//    public static MyPageOrderDetail toMyPageOrderDetailEntity(OrderDetailDto dto) {
//        return MyPageOrderDetail.builder()
//                .orderDetailId(dto.orderDetailId)
//                .itemId(dto.getItemDto().getItemId())
//                .qty(dto.qty)
//                .pricePerOne(dto.pricePerOne)
//                .build() ;
//    }
}
