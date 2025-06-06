package com.study.ecommerce.domain.cart.dto.resp;

import java.util.List;

public record CartResponse(
        Long id,
        Long totalPrice,
        List<CartItemResponse> items
) {
}
