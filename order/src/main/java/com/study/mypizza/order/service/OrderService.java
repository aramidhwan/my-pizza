package com.study.mypizza.order.service;

import com.study.mypizza.order.dto.ItemDto;
import com.study.mypizza.order.dto.OrderDetailDto;
import com.study.mypizza.order.dto.OrderDto;
import com.study.mypizza.order.dto.OrderRequestDto;
import com.study.mypizza.order.entity.Item;
import com.study.mypizza.order.entity.Order;
import com.study.mypizza.order.entity.OrderDetail;
import com.study.mypizza.order.exception.MyPizzaException;
import com.study.mypizza.order.external.InternalGateway;
import com.study.mypizza.order.repository.ItemRepository;
import com.study.mypizza.order.repository.OrderDetailRepository;
import com.study.mypizza.order.repository.OrderRepository;
import com.study.mypizza.order.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)     // 모든 public 메서드에 readOnly=true 적용
public class OrderService {

    private final OrderRepository orderRepository ;
    private final ItemRepository itemRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final InternalGateway internalGateway ;

    @Transactional(rollbackFor = MyPizzaException.class)
    public void orderCancel(Long orderId) throws MyPizzaException {
        String rtnMsg = null ;
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new MyPizzaException("해당 하는 주문이 없습니다. : " + orderId)) ;

        // 주문 접수 이전 단계에서만 취소가능
        if ( order.getStatus() == OrderStatus.ORDERED ) {
            // 취소 전에 주문받은 Store가 있는지 Store MSA에서 다시 한 번 체크
            if (internalGateway.checkOrderCancel(orderId)) {
                order.statusUpdate(OrderStatus.ORDER_CANCELLED);
                orderRepository.save(order) ;
                // 취소 불가능
            } else {
                throw new MyPizzaException("### 주문 취소 불가능 :: 이미 Store에서 준비중입니다.");
            }
        } else {
            // 취소 불가능
            throw new MyPizzaException("### 주문 취소 불가능 :: 주문취소 불가능 단계("+order.getStatus()+") 입니다.") ;
        }
    }

    @Transactional(rollbackFor = MyPizzaException.class)
    public OrderDto createOrder(OrderRequestDto orderRequestDto) throws MyPizzaException {
        List<OrderDetailDto> orderDetailDtos = new ArrayList<>();
        BigDecimal totalPricefromDB = new BigDecimal(0);

        // Req/Res Calling
        // 주문 요청된 지역에 오픈중인 Store가 있는지 체크
        String openYN = internalGateway.checkOpenYn(orderRequestDto.getRegionNm()) ;

        Order newOrder = Order.builder()
                .customerNo(orderRequestDto.getCustomerNo())
                .status("Y".equals(openYN)? OrderStatus.ORDERED:OrderStatus.ORDER_REJECTED)
                .statusInfo("Y".equals(openYN)? "Ordered":"N".equals(openYN)? "OrderRejected::[" + orderRequestDto.getRegionNm() + "] 지역에 영업중인 Store가 없습니다.":"OrderRejected::"+openYN)
                .regionNm(orderRequestDto.getRegionNm())
                .totalPrice(orderRequestDto.getTotalPrice())
                .build();
        newOrder = orderRepository.save(newOrder);

        List<ItemDto> items = orderRequestDto.getItems();
        List<Long> itemIds = new ArrayList<>();
        for (ItemDto itemDto : items) {
            itemIds.add(itemDto.getItemId());
        }
        List<ItemDto> itemDtosFromDB = itemRepository.findByItemIdIn(itemIds)
                .stream()
                .map(ItemDto::of)
                .toList();
        for (ItemDto itemDto : items) {
            Item item = itemRepository.findById(itemDto.getItemId())
                    .orElseThrow(() -> new MyPizzaException("존재하지 않는 품목입니다. ITEM_ID=["+itemDto.getItemId()+"]"));
            totalPricefromDB = totalPricefromDB.add(new BigDecimal(item.getPricePerOne()).multiply(new BigDecimal(itemDto.getQty())));
            orderDetailDtos.add(OrderDetailDto.builder()
                    .orderDto(OrderDto.of(newOrder))
                    .itemDto(itemDto)
                    .qty(itemDto.getQty())
                    .pricePerOne(item.getPricePerOne())
                    .build()
            );
        }

        // DB에 저장되어 계산된 Total 가격과 Parameter로 넘어온 Total 가격이 다를 경우 예외 발생
        if ( totalPricefromDB.intValue() != newOrder.getTotalPrice() ) {
//            newOrder.setTotalPrice(totalPricefromDB.intValue());
//            orderRepository.save(newOrder);
            throw new MyPizzaException("Total 가격이 다릅니다. \n계산된 가격 = ["+totalPricefromDB.intValue()+"] \nParameter 가격 = ["+newOrder.getTotalPrice()+"]") ;
        }
        
        // 주문 상세 내역 저장
        List<OrderDetail> orderDetails = orderDetailRepository.saveAll(
                orderDetailDtos
                .stream()
                .map(OrderDetailDto::toEntity)
                .collect(Collectors.toList())
        ) ;

//        return orderDetails.stream().map(OrderDetailDto::of).toList();
        return OrderDto.of(newOrder) ;
    }
}
