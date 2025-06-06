package com.study.ecommerce.domain.order.dto.resp;

import com.study.ecommerce.domain.order.entity.Order;
import com.study.ecommerce.domain.order.entity.Order.OrderStatus;

public record OrderResponse(
        Long id,
        OrderStatus status,
        Long totalAmount
) {
}
