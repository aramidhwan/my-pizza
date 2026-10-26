package com.study.mypizza.delivery.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Getter
@Slf4j
@RequiredArgsConstructor
public enum OrderStatus {
    ORDERED("Ordered"),
    ORDER_CANCELLED("OrderCancelled"),
    ORDER_REJECTED("OrderRejected"),
    ORDER_ACCEPTED("OrderAccepted"),
    COOKED("Cooked"),
    DELIVERY_ACCEPTED("DeliveryAccepted"),
    DELIVERY_STARTED("DeliveryStarted"),
    DELIVERED("Delivered") ;

    private final String status;

    // 👉 핵심: 다음 상태로의 변경이 유효한지 검증하는 메서드
    public static boolean canTransitionTo(Long orderId, OrderStatus currStatus, OrderStatus nextStatus) {
        if (currStatus == null || nextStatus == null) {
            log.warn("👉 상태(STATUS)가 null 일 수 없습니다! orderId={}, 현재상태:{}, 처리상태:{}", orderId, currStatus, nextStatus);
            return false;
        }
        if (currStatus == nextStatus) {
            log.info("👉 이미 반영된 상태입니다! orderId={}, 현재상태:{}, 처리상태:{}", orderId, currStatus, nextStatus);
            return true ;
        }

        boolean isOk = switch (currStatus) {
            case ORDERED ->
                // 주문됨 상태에서는 '접수', '취소', '거절'로만 이동 가능
                nextStatus == ORDER_ACCEPTED || nextStatus == ORDER_REJECTED || nextStatus == ORDER_CANCELLED;
            case ORDER_ACCEPTED ->
                // 접수됨 상태에서는 '조리완료'로만 이동 가능 (취소 불가)
                nextStatus == COOKED;
            case COOKED -> nextStatus == DELIVERY_ACCEPTED;
            case DELIVERY_ACCEPTED -> nextStatus == DELIVERY_STARTED;
            case DELIVERY_STARTED -> nextStatus == DELIVERED;
            case ORDER_CANCELLED, ORDER_REJECTED, DELIVERED ->
                // 종결된 상태에서는 더 이상 다른 상태로 변경 불가
                    false;
            default -> false;
        };

        if (!isOk) {
            log.warn("👉 STATUS 변경 불가! orderId=[{}], 현재상태:{}, 요청상태:{}", orderId, currStatus, nextStatus);
        }

        return isOk;
    }

    /**
     * status 변환 : OrderAccepted (String) → ORDER_ACCEPTED (Enum)
     * 반대는 valueOf(내장함수)가 있음
     * @param status : OrderAccepted
     * @return : ORDER_ACCEPTED
     */
    public static OrderStatus fromString(String status) {   // status: ORDER_ACCEPTED
        for (OrderStatus s : OrderStatus.values()) {
            if (s.getStatus().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid status: " + status);
    }

    public static boolean isValid(String status) {
        if (status == null) {
            return false;
        }
        return Arrays.stream(OrderStatus.values())
                .anyMatch(s -> s.name().equalsIgnoreCase(status));
    }

    public static boolean isTerminatedStatus(String status) {
        return isTerminatedStatus(OrderStatus.valueOf(status)) ;
    }

    public static boolean isTerminatedStatus(OrderStatus status) {
        return OrderStatus.ORDER_REJECTED == status ||
                OrderStatus.ORDER_CANCELLED == status ||
                OrderStatus.DELIVERED == status;
    }
}
