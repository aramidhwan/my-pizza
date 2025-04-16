package com.study.mypizza.delivery.service;

import com.study.mypizza.delivery.dto.DeliveryDto;
import com.study.mypizza.delivery.entity.Delivery;
import com.study.mypizza.delivery.enums.OrderStatus;
import com.study.mypizza.delivery.event.StatusUpdated;
import com.study.mypizza.delivery.exception.MyPizzaException;
import com.study.mypizza.delivery.external.InternalGateway;
import com.study.mypizza.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    private final DeliveryRepository deliveryRepository ;
    private final InternalGateway internalGateway;

    // 주문 status 업데이트 (배달시작/배달완료 처리)
    @Transactional(rollbackFor = MyPizzaException.class)
    public DeliveryDto updateOrderStatus(StatusUpdated statusUpdated) {
        Delivery delivery = deliveryRepository.findByOrderId(statusUpdated.getOrderId())
                .orElseThrow(() -> new MyPizzaException("주문번호:["+statusUpdated.getOrderId()+"]에 해당하는 주문이 없습니다.")) ;
        if ( statusUpdated.getStatus() == OrderStatus.DELIVERY_STARTED &&
                delivery.getStatus() != OrderStatus.DELIVERY_ACCEPTED ) {
            throw new MyPizzaException("주문번호:["+statusUpdated.getOrderId()+"]가 배달시작(DeliveryStarted) 처리할 상태가 아닙니다.\n현재 상태(Status) : " + delivery.getStatus()) ;
        } else if ( statusUpdated.getStatus() == OrderStatus.DELIVERED &&
                delivery.getStatus() != OrderStatus.DELIVERY_STARTED ) {
            throw new MyPizzaException("주문번호:["+statusUpdated.getOrderId()+"]가 배달완료(Delivered) 처리할 상태가 아닙니다.\n현재 상태(Status) : " + delivery.getStatus()) ;
        }
        delivery.setStatus(statusUpdated.getStatus());
        return DeliveryDto.of(deliveryRepository.save(delivery)) ;
    }
    
//    public Delivery deliveryAcceptedDeliveryId(String status) {
//        List<Delivery> list = deliveryRepository.findByStatus(status);
//        Delivery delivery = null ;
//
//        int cnt = list.size() ;
//        if (cnt > 0) {
//            int random = new Random().nextInt(cnt);
//            delivery = list.get(random);
//        } else {
//            delivery = new Delivery();
//        }
//
//        return delivery ;
//    }

    public List<DeliveryDto> getDeliveryAdmin(int customerNo) {
        return deliveryRepository.findByOwnerNoOrderByStoreIdAscCreateDtDesc(customerNo)
                .stream()
                .map(DeliveryDto::of)
                .map(this::setStoreNm)
//                .peek(deliveryDto -> log.debug("deliveryDto : {}", deliveryDto)) // 🔹 변환 후 확인 가능
                .toList();
    }

    private DeliveryDto setStoreNm(DeliveryDto deliveryDto) {
        if ( deliveryDto.getStoreDto().getStoreId() != null ) {
            deliveryDto.getStoreDto().setStoreNm(internalGateway.getStoreNm(deliveryDto.getStoreDto().getStoreId()));
        } else {
            deliveryDto.getStoreDto().setStoreNm("");
        }
        return deliveryDto;
    }
}
