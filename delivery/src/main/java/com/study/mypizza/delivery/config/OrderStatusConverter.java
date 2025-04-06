package com.study.mypizza.delivery.config;

import com.study.mypizza.delivery.enums.OrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter(autoApply = true) // 모든 OrderStatus 필드에 자동 적용
@Slf4j
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {

    // Enum → String (DB 저장 시)
    @Override
    public String convertToDatabaseColumn(OrderStatus status) {
        if (status == null) {
            return null;
        }
        return status.getStatus(); // ORDERED → "Ordered"
    }

    // String → Enum (DB 조회 시)
    @Override
    public OrderStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return OrderStatus.fromStatus(dbData); // "Ordered" → ORDERED
    }
}

