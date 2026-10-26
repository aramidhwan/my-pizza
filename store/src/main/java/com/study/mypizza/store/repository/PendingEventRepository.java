package com.study.mypizza.store.repository;

import com.study.mypizza.store.entity.PendingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PendingEventRepository extends JpaRepository<PendingEvent, Long> {
    boolean existsByOrderId(long orderId);

    List<PendingEvent> findByOrderIdAndMsaGbAndIsCompletedFalseOrderByEventId(long orderId, String msaGb);
}
