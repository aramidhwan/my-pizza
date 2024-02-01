package com.study.mypizza.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.study.mypizza.order.entity.Order;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path="orders", collectionResourceRel = "orders")
public interface OrderRepository extends JpaRepository<Order, Long> {
}
