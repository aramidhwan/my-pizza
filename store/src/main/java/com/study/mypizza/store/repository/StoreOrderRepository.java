package com.study.mypizza.store.repository;

import com.study.mypizza.store.entity.StoreOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/* @RepositoryRestResource : Repository를 REST API로 자동 노출. 예: /myPages */
//@RepositoryRestResource(path="storeOrders", collectionResourceRel = "storeOrders")
public interface StoreOrderRepository extends JpaRepository<StoreOrder, Long> {
    public Optional<StoreOrder> findByOrderId(Long orderId) ;

    public Long countByStoreStoreId(Long storeId);
}
