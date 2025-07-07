package com.study.ecommerce.domain.order.strategy.shipping;

import com.study.ecommerce.domain.order.entity.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 일반 배송
 */
@Component
public class StandardShippingStrategy implements ShippingStrategy {

    // 기본 배송비
    private static final BigDecimal SHIPPING_COST = new BigDecimal("3000");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50000");
    private static final int DELIVERY_DAYS = 3;

    // 무료배송 기준
    @Override
    public BigDecimal calculateShippingCost(Order order) {
        if(order.getTotalAmount().compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO;
        }
        return SHIPPING_COST;
    }

    @Override
    public String getShippingPolicyName() {
        return "일반배송 (3일)";
    }

    // 배송일자
    @Override
    public int getEstimatedDeliveryDays(Order order) {
        return DELIVERY_DAYS;
    }

}
