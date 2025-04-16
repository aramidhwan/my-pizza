package com.study.mypizza.store.service;

import com.study.mypizza.store.dto.StoreDto;
import com.study.mypizza.store.dto.StoreOrderDetailDto;
import com.study.mypizza.store.dto.StoreOrderDto;
import com.study.mypizza.store.entity.Store;
import com.study.mypizza.store.entity.StoreOrder;
import com.study.mypizza.store.enums.OrderStatus;
import com.study.mypizza.store.exception.MyPizzaException;
import com.study.mypizza.store.external.InternalGateway;
import com.study.mypizza.store.repository.StoreOrderDetailRepository;
import com.study.mypizza.store.repository.StoreOrderRepository;
import com.study.mypizza.store.repository.StoreRepository;
import com.study.mypizza.store.util.MyPizzaAssert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StoreService {
    private final StoreRepository storeRepository ;
    private final StoreOrderRepository storeOrderRepository ;
    private final StoreOrderDetailRepository storeOrderDetailRepository ;
    private final InternalGateway internalGateway;

    public String getStoreNm(Long storeId) {
        String storeNm = storeRepository.findById(storeId)
                .orElseThrow(() -> new MyPizzaException("STORE_ID:["+storeId+"]에 해당하는 상점이 없습니다."))
                .getStoreNm() ;
        log.debug("##### /store/getStoreNm is called in [storeId = {}:{}]", storeId, storeNm) ;
        return storeNm ;
    }

    public String checkOpenYn(String regionNm) {
        log.trace("##### [/store/chkOpenYn] is called in [{}]", regionNm) ;
        String openYN = "N" ;

        try {
            // CircuitBreaker timeout 검증을 위한 임시 코드
//            if ("서초구".equals(regionNm.trim())) {
//                log.debug("##### 서초구 일부러 Exception...");
//                throw new RuntimeException("### 서초구 일부러 Exception");
//            }

            // CircuitBreaker timeout 검증을 위한 임시 코드
            if ("종로구".equals(regionNm.trim())) {
                log.debug("##### 종로구 Sleeping 5s...");
                Thread.sleep(5000);
            }

            List<Store> storeList = storeRepository.findByRegionNmAndOpenYNTrue(regionNm);
            int openStoreCnt = storeList.size();

            // 주문이 들어온 regionNm에 Open된 Sotre가 한군데라도 있으면 true를 리턴
            if (openStoreCnt > 0) {
                openYN = "Y";
            } else {
                openYN = "N";
                log.debug("##### No Store opened in [{}]", regionNm);
            }
        } catch (Throwable t) {
            openYN = t.getMessage();
        }

        return openYN ;
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

    private List<StoreDto> getStoresByNameLike(String storeNm) {
        return storeRepository.findByStoreNmLike(storeNm)
                .stream()
                .map(StoreDto::of)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = MyPizzaException.class)
    public void createModifyStore(StoreDto storeDto) {
        createModifyStore(List.of(storeDto)) ;
    }

    @Transactional(rollbackFor = MyPizzaException.class)
    public void createModifyStore(List<StoreDto> storeDtos) {
        Store storeFromDB = null ;
        ArrayList<StoreDto> existStores = new ArrayList<>();
        for(StoreDto storeDto:storeDtos) {
            existStores.addAll(getStoresByNameLike(storeDto.getStoreNm())) ;
        }

        for(StoreDto storeDto:storeDtos) {
            // 수정
            if ( storeDto.getStoreId() != null ) {
                storeFromDB = storeRepository.findById(storeDto.getStoreId())
                        .orElseThrow(() -> new MyPizzaException("해당하는 Store가 없습니다. STORE_ID=["+storeDto.getStoreId()+"]"));

                if ( !existStores.isEmpty() && !Objects.equals(existStores.get(0).getStoreId(), storeDto.getStoreId())) {
                    throw new MyPizzaException("이미 비슷한 명칭의 Store가 존재합니다. ["+(existStores.size()>0? existStores.get(0).getStoreNm():"")+"]");
                }
                storeRepository.save(storeDto.toEntity()) ;
            // 등록
            } else {
                // 기존 Store 명칭이 있으면 반려
                MyPizzaAssert.isTrue(existStores.isEmpty(),"이미 비슷한 명칭의 Store가 존재합니다. ["+(existStores.size()>0? existStores.get(0).getStoreNm():"")+"]");

                storeRepository.save(storeDto.toEntity()) ;
            }
        }

//        List<Store> stores = storeRepository.saveAll(storeDtos.stream().map(StoreDto::toEntity).toList());
    }

    public List<StoreDto> getStores() {
        List<StoreDto> storeDtos = storeRepository.findAll(PageRequest.of(0,10, Sort.by(ASC,"storeNm","regionNm")))
                .stream()
                .map(StoreDto::of)
                .toList();

        for (StoreDto storeDto:storeDtos) {
            storeDto.setOrderCnt( storeOrderRepository.countByStoreStoreId(storeDto.getStoreId()) ) ;
        }

        return storeDtos ;
    }

    public List<StoreDto> getStoreAdmin(int customerNo) {
//        LocalDateTime now = LocalDateTime.now() ;
//        LocalDateTime today = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0, 0) ;
        LocalDateTime today = LocalDate.now().atStartOfDay();

        List<StoreOrderDetailDto> storeOrderDetailDtos = null ;
        List<StoreDto> storeDtos = storeRepository.findByOwnerNo(customerNo)
                .stream()
                .map(StoreDto::of)
                .toList();

        // storeId -> StoreDto 매핑
        Map<Long, StoreDto> storeDtoMap = storeDtos
                .stream()
                .collect(Collectors.toMap(StoreDto::getStoreId, Function.identity()));

        // 해당 customer의 모든 storeId를 가져온 뒤, 한 번에 주문 조회
        List<Long> storeIds = storeDtos.stream()
                .map(StoreDto::getStoreId)
                .toList();

        // 모든 상점별 주문(StoreOrder) 미리 조회
        List<StoreOrder> allStoreOrders = storeOrderRepository.findByStoreStoreIdInAndCreateDtAfter(storeIds, today);

        // orderId -> StoreOrderDto 매핑
        Map<Long, StoreOrderDto> orderDtoMap = allStoreOrders
                .stream()
                .map(StoreOrderDto::of)
                .collect(Collectors.toMap(StoreOrderDto::getOrderId, Function.identity()));

        // 주문 ID 목록 미리 수집 후, 상세 주문 한 번에 조회
        List<Long> orderIds = allStoreOrders.stream()
                .map(StoreOrder::getOrderId)
                .toList();

        List<StoreOrderDetailDto> allDetails = storeOrderDetailRepository.findByOrderIdIn(orderIds)
                .stream()
                .map(StoreOrderDetailDto::of)
                .peek(dto -> dto.setItemNm(internalGateway.getItemNm(dto.getItemId())))
                .toList();

        // 주문 상세를 주문 ID 기준으로 그룹핑
        Map<Long, List<StoreOrderDetailDto>> orderDetailMap = allDetails.stream()
                .collect(Collectors.groupingBy(StoreOrderDetailDto::getOrderId));

        // 각 주문 DTO에 상세 주문 주입
        for (StoreOrderDto orderDto : orderDtoMap.values()) {
            orderDto.setStoreOrderDetailDtos(orderDetailMap.getOrDefault(orderDto.getOrderId(), List.of()));
        }

        // 주문들을 storeId 기준으로 그룹핑
        Map<Long, List<StoreOrderDto>> storeOrdersMap = orderDtoMap.values().stream()
                .collect(Collectors.groupingBy(orderDto -> {
                    StoreOrder storeOrder = allStoreOrders.stream()
                            .filter(o -> o.getOrderId().equals(orderDto.getOrderId()))
                            .findFirst().orElse(null);
                    return storeOrder != null ? storeOrder.getStore().getStoreId() : null;
                }));

        // storeDto에 주문 주입
        for (StoreDto storeDto : storeDtos) {
            storeDto.setStoreOrderDtos(storeOrdersMap.getOrDefault(storeDto.getStoreId(), List.of()));
        }

        return storeDtos;

//        ---------------------------------
//        for (StoreDto storeDto:storeDtos) {
//            List<StoreOrder> storeOrders = storeOrderRepository.findByStoreStoreIdAndCreateDtAfterOrderByOrderIdAsc(storeDto.getStoreId(), today) ;
//
//            storeDto.setStoreOrderDtos(storeOrders
//                    .stream()
////                    .peek(order -> log.debug("### status : " + order.getStatus()))
//                    .map(StoreOrderDto::of)
//                    .toList()) ;
//
//            for ( StoreOrderDto storeOrderDto : storeDto.getStoreOrderDtos() ) {
//                storeOrderDetailDtos = storeOrderDetailRepository.findByOrderIdOrderByItemId(storeOrderDto.getOrderId())
//                        .stream()
//                        .map(StoreOrderDetailDto::of)
//                        .toList();
//
//                for (StoreOrderDetailDto storeOrderDetailDto : storeOrderDetailDtos ) {
//                    // Cache 처리된 setItemNm 메소드 호출
//                    storeOrderDetailDto.setItemNm(orderGateway.getItemNm(storeOrderDetailDto.getItemId()));
//                }
//
//                storeOrderDto.setStoreOrderDetailDtos(storeOrderDetailDtos) ;
//            }
//        }
//
//        return storeDtos ;
    }

    @Transactional
    public StoreDto udpateStore(StoreDto storeDto) {
        return StoreDto.of(storeRepository.save(storeDto.toEntity())) ;
    }

    @Transactional(rollbackFor = MyPizzaException.class)
    public void deleteStore(Long storeId) {
        Store storeFromDB = storeRepository.findById(storeId)
                .orElseThrow(() -> new MyPizzaException("STORE ID=["+storeId+"]에 해당하는 상점이 없습니다.")) ;

        if ( storeFromDB.getOpenYN() ) {
            throw new MyPizzaException("영업중인 Store는 삭제할 수 없습니다. ["+storeFromDB.getStoreNm()+"]") ;
        } else {
            storeRepository.deleteById(storeId);
        }
    }

    // 주문 status 업데이트 (조리완료 처리)
    @Transactional(rollbackFor = MyPizzaException.class)
    public StoreOrderDto updateOrderStatus(StoreOrderDto storeOrderDto) {
        StoreOrder storeOrder = storeOrderRepository.findByOrderId(storeOrderDto.getOrderId())
                .orElseThrow(() -> new MyPizzaException("주문번호:["+storeOrderDto.getOrderId()+"]에 해당하는 주문이 없습니다.")) ;

        if ( storeOrder.getStatus() != OrderStatus.ORDER_ACCEPTED ) {
            throw new MyPizzaException("주문번호:["+storeOrderDto.getOrderId()+"]가 조리완료(Cooked) 처리할 상태가 아닙니다.\n현재 상태(Status) : " + storeOrderDto.getStatus()) ;
        }
        storeOrder.setStatus(storeOrderDto.getStatus());
        return StoreOrderDto.of(storeOrderRepository.save(storeOrder)) ;
    }
}
