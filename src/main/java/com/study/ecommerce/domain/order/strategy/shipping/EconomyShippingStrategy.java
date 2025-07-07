package com.study.ecommerce.domain.order.strategy.shipping;

import com.study.ecommerce.domain.order.entity.Order;

import java.math.BigDecimal;

public class EconomyShippingStrategy implements ShippingStrategy {
    // 배송비
    private static final BigDecimal SHIPPING_COST = new BigDecimal("1500");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("30000");
    private static final int DELIVERY_DAYS = 5;

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
        return "이코노미 배송 (5-7일)";
    }

    // 배송날짜
    @Override
    public int getEstimatedDeliveryDays(Order order) {
        return DELIVERY_DAYS;
    }




}
