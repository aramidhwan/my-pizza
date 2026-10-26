package com.study.mypizza.order.service;

import com.study.mypizza.order.dto.*;
import com.study.mypizza.order.entity.Order;
import com.study.mypizza.order.event.*;
import com.study.mypizza.order.exception.MyPizzaException;
import com.study.mypizza.order.external.InternalGateway;
import com.study.mypizza.order.repository.ItemRepository;
import com.study.mypizza.order.repository.OrderRepository;
import com.study.mypizza.order.enums.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
    private final InternalGateway internalGateway ;
    private final CacheService cacheService;

    private final EntityManager entityManager;

    @Transactional
    public void orderCancel(Long orderId) throws MyPizzaException {
        String rtnMsg = null ;
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new MyPizzaException("⚠️ 해당 하는 주문이 없습니다. : " + orderId)) ;

        // 주문 접수 이전 단계에서만 취소가능
        if ( order.getStatus() == OrderStatus.ORDERED ) {
            // 취소 전에 주문받은 Store가 있는지 Store MSA에서 다시 한 번 체크
            if (internalGateway.checkOrderCancel(orderId)) {
                order.updateStatus(OrderStatus.ORDER_CANCELLED);
                orderRepository.flush();
                // 취소 불가능
            } else {
                throw new MyPizzaException("⚠️ 주문 취소 불가능 :: 이미 상점에서 준비중입니다.");
            }
        } else {
            // 취소 불가능
            throw new MyPizzaException("⚠️ 주문 취소 불가능 :: 주문취소 불가능 단계("+order.getStatus()+") 입니다.") ;
        }
    }

    @Transactional(readOnly = true)
    public List<String> getOpenRegionNmList() throws MyPizzaException {
        // 1. Store MSA에서 regionNmList를 얻어온다.
        // Req/Res Calling
        return cacheService.getOpenRegionNmList() ;
    }

    @Transactional
    public OrderDto createOrder(OrderRequestDto orderRequestDto) throws MyPizzaException {
        // 1. 주문 요청된 지역에 오픈중인 Store가 있는지 체크
        // Req/Res Calling
        String openYN = "Y" ;
//        String openYN = cacheService.checkOpenYn(orderRequestDto.getRegionNm()) ;

        // 2-1. 주문 수량 오류 체크
        List<ItemDto> itemsFromRequest = orderRequestDto.getItems();
        itemsFromRequest.forEach(itemDto -> {
            if (itemDto.getQty() < 1)
                throw new MyPizzaException("⚠️ 주문 수량이 잘못되었습니다. [" + itemDto.getItemNm() + "]=[" + itemDto.getQty() + "개]") ;
        });

        // 2-1. Total 가격 검증용 데이터 추출
        List<ItemDto> itemsFromDB = itemRepository.findByItemIdIn(orderRequestDto.getItems().stream().map(ItemDto::getItemId).toList())
                .stream()
                .map(ItemDto::of)
                .toList();
        if ( itemsFromDB.size() != itemsFromRequest.size() ) {
            throw new MyPizzaException("⚠️ 존재하지 않는 메뉴가 있습니다. itemCd=[" + itemsFromRequest + "]") ;
        }
        // orderRequestDto의 itemId별 qty를 Map으로 변환
        Map<Long, Integer> qtyMap = orderRequestDto.getItems().stream()
                                                    .collect(Collectors.toMap(
                                                            ItemDto::getItemId,
                                                            ItemDto::getQty,
                                                            (oldValue, newValue) -> newValue // 혹시 모를 중복 itemId 처리
                                                    ));
        // itemsFromDB를 순회하며 orderRequestDto의 qty 값으로 셋팅 후 totalPricefromDB 계산
        BigDecimal totalPricefromDB = new BigDecimal(0) ;
        for (ItemDto dbDto : itemsFromDB) {
            Integer requestQty = qtyMap.get(dbDto.getItemId());
            if (requestQty != null) {
                dbDto.setQty(requestQty);
            }
            totalPricefromDB = totalPricefromDB.add(new BigDecimal(dbDto.getPricePerOne()).multiply(new BigDecimal(dbDto.getQty())));
        }

        // 2-2. Total 가격 검증
        // DB에 저장되어 계산된 Total 가격과 Parameter로 넘어온 Total 가격이 다를 경우 예외 발생
        if ( totalPricefromDB.intValue() != orderRequestDto.getTotalPrice() ) {
            throw new MyPizzaException("⚠️ Total 가격이 다릅니다. \n계산된 가격 = ["+totalPricefromDB.intValue()+"] \nParameter 가격 = ["+orderRequestDto.getTotalPrice()+"]") ;
        }

        // Order Entity에 넣을 orderDetailDtoList 만들기
        List<OrderDetailDto> orderDetailDtoList = new ArrayList<>();
        // orderRequestDto의 itemId별 qty를 Map으로 변환 (DB의 주문단가로 셋팅)
        Map<Long, Integer> pricePerOneMap = orderRequestDto.getItems().stream()
                .collect(Collectors.toMap(
                        ItemDto::getItemId,
                        ItemDto::getPricePerOne,
                        (oldValue, newValue) -> newValue // 혹시 모를 중복 itemId 처리
                ));
        for (ItemDto itemDto : itemsFromRequest) {
            orderDetailDtoList.add(OrderDetailDto.builder()
//                    .orderDto(OrderDto.of(newOrder))
                            .itemDto(itemDto)
                            .qty(itemDto.getQty())
                            .pricePerOne(pricePerOneMap.get(itemDto.getItemId()))
                            .build()
            );
        }

        // 3. 주문(Order, OrderDetail) 만들기
        Order newOrder = Order.builder()
                .customerNo(orderRequestDto.getCustomerNo())
                .status(OrderStatus.ORDERED)
                .statusInfo("Ordered")
                .regionNm(orderRequestDto.getRegionNm())
                .totalPrice(orderRequestDto.getTotalPrice())
                .build();
        // 4. 주문 상세(OrderDetail) 만들기
        newOrder.assignOrderDetail(orderDetailDtoList.stream()
                .map(OrderDetailDto::toEntity)
                .toList());

        // openYN 처리
        if ( !"Y".equals(openYN) ) {
            // 상태 업데이트 (ORDER_REJECTED)
            if (newOrder.updateStatus(OrderStatus.ORDER_REJECTED) ) {
                String msg = "N".equals(openYN)? "[" + orderRequestDto.getRegionNm() + "] 지역에 영업중인 Store가 없습니다.":openYN ;
                log.warn(msg);
                newOrder.appendStatusInfo(msg);
            }
        }

        // 5. 주문 및 주문상세 (Order, OrderDetail) DB 저장
        OrderDto newOrderDto = OrderDto.of(orderRepository.save(newOrder));

        // 4. 통합 이벤트 생성 (추후에는 Transaction Outbox 패턴 고려)
        publishEvent(newOrderDto) ;

        return newOrderDto ;
    }

    // kafka 이벤트(Ordered) 발행
    private void publishEvent(OrderDto orderDto) {
        if (orderDto.getStatus() == null) {
            log.error("⚠️ publishEvent 주문 상태 이상..!! orderId=[{}], status=null", orderDto.getOrderId());
            return;
        }

        AbstractEvent event = null;
        switch (orderDto.getStatus()) {
            case ORDERED -> event = new Ordered();
            case ORDER_REJECTED -> event = new OrderRejected();
            default -> {
                String msg = "⚠️ UNKNOWN 이벤트!! : orderId=["+orderDto.getOrderId()+"], "+orderDto.getStatus() ;
                log.error(msg);
                throw new MyPizzaException(msg) ;
            }
        }

        // 5. 현재 DB 트랜잭션 커밋 이후 한 번만 발행
        BeanUtils.copyProperties(orderDto, event);
        event.publishAfterCommit();
        log.info("👉 카프카 이벤트 발송..!! : {}", event);
    }

    @Transactional
    public void delOrderData() {
        // FK 제약 때문에 자식 테이블을 먼저 비웁니다.
        entityManager.createNativeQuery("DELETE FROM t_orderdetail").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM t_order").executeUpdate();

        // t_order가 비어 있는 상태에서 다음 AUTO_INCREMENT 값을 1로 설정합니다.
        entityManager.createNativeQuery("ALTER TABLE t_order AUTO_INCREMENT = 1")
                .executeUpdate();

        log.info("👉 주문 데이터 삭제 및 초기화 완료!") ;
    }

    /**
     * STORE MSA에서 호출되는 메소드로 ORDERED 건에 대해 주문 할당된 storeId 셋팅
     * @param orderId
     * @param storeId
     */
    @Transactional
    public void assignStoreId(Long orderId, Long storeId) {
        // 기존 주문(Order) 검색
        Optional<Order> orderOptional = orderRepository.findById(orderId) ;
        if (orderOptional.isEmpty()) {
            log.error("⚠️ 주문 테이블(t_order)에 해당 하는 주문이 없습니다! orderId=[{}]", orderId);
            return ;
        }

        Order order = orderOptional.get();
        order.assignStore(storeId) ;
        log.info("👉 주문 건(ORDERED)에 대한 상점번호 할당됨! : orderId=[{}], 상점번호=[{}]", orderId, storeId);
    }
}
