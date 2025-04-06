package com.study.mypizza.store.repository;

import com.study.mypizza.store.entity.StoreOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path="storeOrderDetails", collectionResourceRel = "storeOrderDetails")
public interface StoreOrderDetailRepository extends JpaRepository<StoreOrderDetail, Long> {
    List<StoreOrderDetail> findByOrderIdOrderByItemId(Long orderId);

    List<StoreOrderDetail> findByOrderIdIn(List<Long> orderIds);
}
