package com.study.ecommerce.domain.order.strategy.shipping;

import com.study.ecommerce.domain.order.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpressShippingStrategy implements ShippingStrategy {
    private static final LocalDate TODAY = LocalDate.now();
    private static final BigDecimal SHIPPING_COST = new BigDecimal("10000");
    private static final BigDecimal TOTAL_AMOUNT = new BigDecimal("50000");

    // 배송비
    @Override
    public BigDecimal calculateShippingCost(Order order) {
        if(getEstimatedDeliveryDays(order) == 1) {
            return BigDecimal.ZERO;
        }
        return SHIPPING_COST;
    }

    @Override
    public String getShippingPolicyName() {
        return "익스프레스 배송";
    }

    // 당일인지 익일인지 = 1
    @Override
    public int getEstimatedDeliveryDays(Order order) {
        return order.getOrderDate().toLocalDate().isEqual(TODAY) ? 1 : 0;
    }

}
