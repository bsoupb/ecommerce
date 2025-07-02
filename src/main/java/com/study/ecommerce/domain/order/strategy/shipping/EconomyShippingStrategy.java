package com.study.ecommerce.domain.order.strategy.shipping;

import com.study.ecommerce.domain.order.entity.Order;

import java.math.BigDecimal;

public class EconomyShippingStrategy implements ShippingStrategy {
    // 배송비
    private static final BigDecimal SHIPPING_COST = new BigDecimal("3000");
    private static final BigDecimal TOTAL_AMOUNT = new BigDecimal("20000");

    // 무료배송 기준
    @Override
    public BigDecimal calculateShippingCost(Order order) {
        if(getEstimatedDeliveryDays(order) == 20) {
            return BigDecimal.ZERO;
        }
        return SHIPPING_COST;
    }

    @Override
    public String getShippingPolicyName() {
        return "이코노미 배송";
    }

    // 배송날짜
    @Override
    public int getEstimatedDeliveryDays(Order order) {
        return order.getTotalAmount().compareTo(TOTAL_AMOUNT) >= 0 ? 20 : 15;
    }




}
