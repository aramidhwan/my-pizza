package com.study.mypizza.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.study.mypizza.order.entity.Order;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RepositoryRestResource(path="orders", collectionResourceRel = "orders")
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByStatusAndCreateDtAfter(String status, LocalDateTime createDt) ;
}
