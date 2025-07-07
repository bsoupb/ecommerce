package com.study.ecommerce.domain.order.service;

import com.study.ecommerce.domain.order.dto.req.OrderCreateRequest;
import com.study.ecommerce.domain.order.dto.resp.OrderDetailResponse;
import com.study.ecommerce.domain.order.dto.resp.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface OrderService {
    OrderResponse createOrder(OrderCreateRequest request, String email);
    OrderResponse cancelOrder(Long orderId, String email);
    OrderDetailResponse getOrderDetail(Long orderId, String email);
    Page<OrderResponse> getOrders(String email, Pageable pageable);

    BigDecimal getTodayOrderAmount(Long memberId);
    BigDecimal calculateTotalAmount(OrderCreateRequest request);

}
