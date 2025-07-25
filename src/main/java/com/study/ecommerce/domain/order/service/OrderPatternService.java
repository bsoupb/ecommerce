package com.study.ecommerce.domain.order.service;

import com.study.ecommerce.domain.order.dto.req.OrderCreateRequest;
import com.study.ecommerce.domain.order.entity.Order;
import com.study.ecommerce.domain.order.event.OrderCreatedEvent;
import com.study.ecommerce.domain.order.event.OrderPaidEvent;
import com.study.ecommerce.domain.order.templete.OrderProcessTemplate;
import com.study.ecommerce.domain.order.templete.PremiumOrderProcessor;
import com.study.ecommerce.domain.order.templete.RegularOrderProcessor;
import com.study.ecommerce.domain.order.validation.OrderValidationChain;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderPatternService {
    private final RegularOrderProcessor regularOrderProcessor;
    private final PremiumOrderProcessor premiumOrderProcessor;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderValidationChain validationChain;

    public Order createOrder(OrderCreateRequest request) {
        try {
            validationChain.validateOrder(request);

            OrderProcessTemplate orderProcessTemplate = selectOrderProcessor(request);
            Order order = orderProcessTemplate.processOrder(request);

            publishOrderCreateEvent(order);
            publishOrderPaidEvent(order, request.paymentMethod());

            return order;
        } catch (Exception e) {
            log.error("에러메시지: {}", e.getMessage());
            throw e;
        }
    }

    private OrderProcessTemplate selectOrderProcessor(OrderCreateRequest request) {
        boolean isVipMember = isVipMember(request.memberId());

        if(isVipMember) {
            return premiumOrderProcessor;
        } else {
            return regularOrderProcessor;
        }
    }

    private boolean isVipMember(@NotNull Long memberId) {
        return memberId % 10 == 0;
    }

    private void publishOrderCreateEvent(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(this, order);
        eventPublisher.publishEvent(event);
    }

    private void publishOrderPaidEvent(Order order, String paymentMethod) {
        OrderPaidEvent orderPaidEvent = new OrderPaidEvent(this, order, paymentMethod);
        eventPublisher.publishEvent(orderPaidEvent);
    }
}
