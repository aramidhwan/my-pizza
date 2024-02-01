package com.study.mypizza.delivery.eventHandler.workers;

import com.study.mypizza.delivery.repository.DeliveryRepository;
import com.study.mypizza.delivery.entity.Delivery;
import com.study.mypizza.delivery.event.Cooked;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Setter
public class DeliveryAcceptWorkerThread implements Runnable {
    @Autowired
    private DeliveryRepository deliveryRepository;

    private Cooked cooked ;

    @Override
    public void run() {
        deliveryAccept();
    }

    // Store에서 조리완료 시 배달 신규 생성(DeliveryAccepted)
    private void deliveryAccept() {
        Delivery delivery = new Delivery();
        BeanUtils.copyProperties(cooked, delivery);
        delivery.setStatus("DeliveryAccepted");
        deliveryRepository.save(delivery);
    }
}