package com.study.mypizza.store.repository;

import com.study.mypizza.store.entity.StoreOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/* @RepositoryRestResource : Repository를 REST API로 자동 노출. 예: /myPages */
//@RepositoryRestResource(path="storeOrderDetails", collectionResourceRel = "storeOrderDetails")
public interface StoreOrderDetailRepository extends JpaRepository<StoreOrderDetail, Long> {
    List<StoreOrderDetail> findByOrderIdOrderByItemId(Long orderId);

    List<StoreOrderDetail> findByOrderIdIn(List<Long> orderIds);
}
