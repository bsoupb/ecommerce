package com.study.ecommerce.domain.cart.dto.resp;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        Long price,
        Integer quantity,
        Long totalPrice
) {
}
