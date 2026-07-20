package com.study.mypizza.delivery.repository;

import com.study.mypizza.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/* @RepositoryRestResource : Repository를 REST API로 자동 노출. 예: /myPages */
//@RepositoryRestResource(path="deliveries")
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByStatus(String status) ;

    List<Delivery> findByOwnerNoOrderByStoreIdAscCreateDtDesc(int customerNo);
    List<Delivery> findAllByOrderByStoreIdAscCreateDtDesc();

    Optional<Delivery> findByOrderId(Long orderId);

    List<Delivery> findAllByCreateDtBetweenOrderByStoreIdAscCreateDtDesc(LocalDateTime startDate, LocalDateTime endDate);
}
