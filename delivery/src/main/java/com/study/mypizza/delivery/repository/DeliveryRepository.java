package com.study.mypizza.delivery.repository;

import com.study.mypizza.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path="deliveries")
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
