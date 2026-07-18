package com.study.mypizza.order.repository;

import com.study.mypizza.order.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/* @RepositoryRestResource : Repository를 REST API로 자동 노출. 예: /myPages */
//@RepositoryRestResource(path="orderDetails", collectionResourceRel = "orderDetails")
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}