package com.study.mypizza.delivery.repository;

import com.study.mypizza.delivery.dto.DeliveryDto;
import com.study.mypizza.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path="deliveries")
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByStatus(String status) ;

    List<Delivery> findByOwnerNoOrderByStoreIdAscCreateDtDesc(int customerNo);

    Optional<Delivery> findByOrderId(Long orderId);
}
