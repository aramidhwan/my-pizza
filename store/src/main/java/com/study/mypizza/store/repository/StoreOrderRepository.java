package com.study.mypizza.store.repository;

import com.study.mypizza.store.entity.StoreOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.Optional;

@RepositoryRestResource(path="storeOrders", collectionResourceRel = "storeOrders")
public interface StoreOrderRepository extends JpaRepository<StoreOrder, Long> {
    public Optional<StoreOrder> findByOrderId(Long orderId) ;
}
