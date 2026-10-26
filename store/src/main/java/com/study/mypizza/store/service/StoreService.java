package com.study.mypizza.store.service;

import com.study.mypizza.store.dto.GatewayDto;
import com.study.mypizza.store.dto.StoreDto;
import com.study.mypizza.store.dto.StoreOrderDto;
import com.study.mypizza.store.entity.Store;
import com.study.mypizza.store.entity.StoreOrder;
import com.study.mypizza.store.enums.OrderStatus;
import com.study.mypizza.store.event.*;
import com.study.mypizza.store.exception.MyPizzaException;
import com.study.mypizza.store.mapper.StoreMapper;
import com.study.mypizza.store.repository.StoreOrderDetailRepository;
import com.study.mypizza.store.repository.StoreOrderRepository;
import com.study.mypizza.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StoreService {
    private final StoreRepository storeRepository ;
    private final StoreOrderRepository storeOrderRepository ;
    private final StoreOrderDetailRepository storeOrderDetailRepository ;
    private final CacheService cacheService;
    private final StoreMapper storeMapper;

    public GatewayDto<List<String>> getOpenRegionNmList() {
        log.debug("■■■ /store/getOpenRegionNmList is called.") ;
        List<String> regionNmList = storeRepository.findActiveRegionNmList(true) ;
        return GatewayDto.<List<String>>builder()
                .dto(regionNmList)
                .build();
    }

    public GatewayDto<StoreDto> getStoreNm(Long storeId) {
        StoreDto storeDto = storeRepository.findById(storeId)
                .map(StoreDto::of)
                .orElseThrow(() -> new MyPizzaException("⚠️ STORE_ID:["+storeId+"]에 해당하는 상점이 없습니다."))
                ;
        log.debug("##### /store/getStoreNm is called in [storeId = {}:{}]", storeId, storeDto.getStoreNm()) ;
        return GatewayDto.<StoreDto>builder()
                .dto(storeDto)
                .build();
    }

    public GatewayDto<String> checkOpenYn(String regionNm) {
        log.info("##### [/store/chkOpenYn] is called in [{}]", regionNm) ;
        String openYN = "N" ;

        try {
            // CircuitBreaker timeout 검증을 위한 임시 코드
//            if ("서초구".equals(regionNm.trim())) {
//                log.warn("##### 서초구 일부러 Exception for CircuitBreak of ORDER MSA.");
//                throw new RuntimeException("### 서초구 일부러 Exception for CircuitBreak of ORDER MSA.");
//            }

            // CircuitBreaker timeout 검증을 위한 임시 코드
//            if ("종로구".equals(regionNm.trim())) {
//                log.warn("##### 종로구 Sleeping 5s...");
//                Thread.sleep(5000);
//            }

            List<Store> storeList = storeRepository.findByRegionNmAndOpenYNTrue(regionNm);
            int openStoreCnt = storeList.size();

            // 주문이 들어온 regionNm에 Open된 Sotre가 한군데라도 있으면 true를 리턴
            if (openStoreCnt > 0) {
                openYN = "Y";
            } else {
                openYN = "N";
                log.warn("##### No Store opened in [{}]", regionNm);
            }
        } catch (Throwable t) {
            openYN = t.getMessage();
        }

        return GatewayDto.<String>builder()
                .dto(openYN)
                .build();
    }

    public boolean checkOrderCancel(Long orderId) {
        boolean cancelled = false ;
        Optional<StoreOrder> optional = storeOrderRepository.findByOrderId(orderId) ;

        // StoreOrder에 해당 orderId가 없을 경우 "주문취소" 가능
        if (optional.isEmpty()) {
            cancelled = true ;
        }

        return cancelled ;
    }

//    public StoreOrder getOrderAcceptedStoreOrderId() {
//        List<StoreOrder> list = storeOrderRepository.findByStatus("OrderAccepted") ;
//        StoreOrder storeOrder = null ;
//
//        int cnt = list.size() ;
//        if (cnt > 0) {
//            int random = new Random().nextInt(cnt);
//            storeOrder = list.get(random);
//        } else {
//            storeOrder = StoreOrder.builder().build();
//        }
//
//        return storeOrder ;
//    }

//    public List<StoreOrderDto> getStoreOrders(Long storeId, String status) {
//        return storeOrderRepository.findByStoreStoreIdAndStatusOrderByCreateDtAsc(storeId, status)
//                .stream()
//                .map(StoreOrderDto::of)
//                .toList();
//    }

    public List<StoreDto> getStores(boolean isRoleAdmin, int ownerNo) {
        List<StoreDto> storeDtoList = null ;

        // ROLE_ADMIN 인 경우 전체 조회
        if (isRoleAdmin) {
            storeDtoList = storeRepository.findAll(PageRequest.of(0,10, Sort.by(ASC,"storeNm","regionNm")))
                    .stream()
                    .map(StoreDto::of)
                    .toList();
        // ROLE_STORE_ADMIN 인 경우 로그인 사용자가 owner인 상점만 조회
        } else {
            storeDtoList = storeRepository.findByOwnerNoOrderByStoreNmAscRegionNmAsc(ownerNo)
                    .stream()
                    .map(StoreDto::of)
                    .toList();
        }

        for (StoreDto storeDto:storeDtoList) {
            storeDto.setOrderCnt( storeOrderRepository.countByStoreStoreId(storeDto.getStoreId()) ) ;
        }

        return storeDtoList ;
    }

    /**
     * 상점 Owner 페이지
     * 로그인된 사용자가 Owner인 상점 리스트와 각 상점별 주문 리스트를 가져온다.
     * @param ownerNo
     * @param startDate (주문일)
     * @param endDate (주문일)
     * @return List<StoreDto>
     */
    public List<StoreDto> getStoreAdmin(int ownerNo, String startDate, String endDate) {
        // 변환: 하루의 시작부터 끝까지 포함되도록
        LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
        List<StoreDto> storeDtoList = storeMapper.selectStoreAdmin(ownerNo, startDateTime, endDateTime) ;
        storeDtoList.stream()
                .filter(store -> store.getStoreOrderDtoList() != null)
                .flatMap(store -> store.getStoreOrderDtoList().stream())
                .filter(storeOrder -> storeOrder.getStoreOrderDetailDtoList() != null)
                .flatMap(storeOrder -> storeOrder.getStoreOrderDetailDtoList().stream())
                .filter(detail -> Objects.nonNull(detail.getItemId()) && !Objects.equals(detail.getItemId(), 0L))
                .forEach(detail -> detail.setItemNm(cacheService.getItemNm(detail.getItemId())));
//        storeDtoList.forEach(storeDto -> log.trace("{}", storeDto));
        if (log.isTraceEnabled()) {
            storeDtoList.stream().map(StoreDto::toString).forEach(log::trace);
        }
        return storeDtoList;
    }

    // 주문 status 업데이트
    @Transactional
    public StoreOrderDto updateOrderStatus(StoreOrderDto storeOrderDto) {
        StoreOrder storeOrder = storeOrderRepository.findByOrderId(storeOrderDto.getOrderId())
                .orElseThrow(() -> new MyPizzaException("⚠️ 주문번호:["+storeOrderDto.getOrderId()+"]에 해당하는 주문이 없습니다.")) ;

        // 아래 comment 처리된 부분과 바꿔야 함 ("kafka 테스트" 버튼 때문에 임시임)
        boolean canTransition = OrderStatus.canTransitionTo(storeOrder.getOrderId(), storeOrder.getStatus(), storeOrderDto.getStatus()) ;
        if (!"SKIP_STATUS_CHECK".equals(storeOrderDto.getForTest()) && !canTransition) {
            String msg = "⚠️ 주문번호:[" + storeOrder.getOrderId() + "]가 "+storeOrderDto.getStatus().getStatus()+" 처리할 상태가 아닙니다.\n현재 상태(Status) : " + storeOrder.getStatus() ;
            log.error(msg) ;
            throw new MyPizzaException(msg) ;
        }
        storeOrder.setStatus(storeOrderDto.getStatus());
//        boolean isSucceeded = storeOrder.updateStatus(storeOrderDto.getStatus()) ;
//        if (!isSucceeded) {
//            String msg = "⚠️ 주문번호:[" + storeOrder.getOrderId() + "]가 "+storeOrderDto.getStatus().getStatus()+" 처리할 상태가 아닙니다.\n현재 상태(Status) : " + storeOrder.getStatus() ;
//            log.error(msg) ;
//            throw new MyPizzaException(msg) ;
//        }

        // 반드시 2줄로 분리해서 실행해야 갱신된 updateDt 값이 StoreOrderDto 에 담겨서 리턴된다.
        // 이미 조회된 엔티티는 영속 상태이므로 save()를 생략하고 flush만 호출해도 됩니다.
        // SQL UPDATE 및 @LastModifiedDate 반영
        storeOrderRepository.flush();
        publishEvent(storeOrder);
        return StoreOrderDto.of(storeOrder);
    }

    // kafka 이벤트(Ordered) 발행
    private void publishEvent(StoreOrder storeOrder) {
        AbstractEvent event = null ;

        if (OrderStatus.ORDER_ACCEPTED == storeOrder.getStatus()) {
            OrderAccepted orderAccepted = new OrderAccepted() ;
            BeanUtils.copyProperties(storeOrder, orderAccepted);
            orderAccepted.setStoreId(storeOrder.getStore().getStoreId());
            event = orderAccepted ;
        } else if (OrderStatus.ORDER_REJECTED == storeOrder.getStatus()) {
            OrderRejected orderRejected = new OrderRejected();
            BeanUtils.copyProperties(storeOrder, orderRejected);
            orderRejected.setStoreId(storeOrder.getStore().getStoreId());
            event = orderRejected ;
            // 조리완료 처리
        } else if (OrderStatus.COOKED == storeOrder.getStatus()) {
            Cooked cooked = new Cooked() ;
            BeanUtils.copyProperties(storeOrder, cooked);
            cooked.setStoreId(storeOrder.getStore().getStoreId());
            event = cooked ;
            // 접수취소 처리
        } else if (OrderStatus.ORDER_CANCELLED == storeOrder.getStatus()) {
            OrderCancelled orderCancelled = new OrderCancelled() ;
            BeanUtils.copyProperties(storeOrder, orderCancelled);
            event = orderCancelled ;
        } else {
            String msg = "⚠️ 카프카 이벤트 발송에서 알 수 없는 상태입니다. orderId=[" + storeOrder.getOrderId() + "], " + storeOrder.getStatus() ;
            log.error(msg) ;
            throw new MyPizzaException(msg) ;
        }

        event.publishAfterCommit();
        log.info("👉 카프카 이벤트 발송..!! : {}", event);
    }
}
