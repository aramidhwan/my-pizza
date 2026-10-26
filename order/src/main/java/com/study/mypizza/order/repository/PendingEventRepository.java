package com.study.mypizza.order.repository;

import com.study.mypizza.order.entity.PendingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PendingEventRepository extends JpaRepository<PendingEvent, Long> {
    boolean existsByOrderId(long orderId);

    List<PendingEvent> findByOrderIdAndMsaGbAndIsCompletedFalseOrderByEventId(long orderId, String msaGb);
}
