package com.study.ecommerce.domain.order.strategy.shipping;

import com.study.ecommerce.domain.order.entity.Order;

import java.math.BigDecimal;

public class StandardShippingStrategy implements ShippingStrategy {

    // 기본 배송비
    private static final BigDecimal SHIPPING_COST = new BigDecimal("5000");
    private static final BigDecimal TOTAL_AMOUNT = new BigDecimal("30000");

    // 무료배송 기준
    @Override
    public BigDecimal calculateShippingCost(Order order) {
        if(getEstimatedDeliveryDays(order) == 5) {
            return BigDecimal.ZERO;
        }
        return SHIPPING_COST;
    }

    @Override
    public String getShippingPolicyName() {
        return "";
    }

    // 배송일자
    @Override
    public int getEstimatedDeliveryDays(Order order) {
        return order.getTotalAmount().compareTo(TOTAL_AMOUNT) >= 0 ? 3 : 5;
    }

}
