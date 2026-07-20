package com.study.mypizza.store.repository;

import com.study.mypizza.store.entity.StoreOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/* @RepositoryRestResource : Repository를 REST API로 자동 노출. 예: /myPages */
//@RepositoryRestResource(path="storeOrders", collectionResourceRel = "storeOrders")
public interface StoreOrderRepository extends JpaRepository<StoreOrder, Long> {
    public Optional<StoreOrder> findByOrderId(Long orderId) ;
    public List<StoreOrder> findByStatus(String status) ;

    public Long countByStoreStoreId(Long storeId);

    List<StoreOrder> findByStoreStoreIdAndStatusOrderByCreateDtAsc(Long storeId, String status);

    List<StoreOrder> findByStoreStoreIdAndCreateDtAfterOrderByOrderIdAsc(Long storeId, LocalDateTime today);

    List<StoreOrder> findByStoreStoreIdInAndCreateDtAfter(List<Long> storeIds, LocalDateTime today);

    List<StoreOrder> findByStoreStoreIdInAndCreateDtBetween(List<Long> storeIds, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
