package com.study.mypizza.mypage.enums;

import lombok.Getter;

@Getter
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

    // 생성자
    OrderStatus(String status) {
        this.status = status;
    }

    public static OrderStatus fromStatus(String status) {
        for (OrderStatus s : OrderStatus.values()) {
            if (s.getStatus().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid status: " + status);
    }
}
