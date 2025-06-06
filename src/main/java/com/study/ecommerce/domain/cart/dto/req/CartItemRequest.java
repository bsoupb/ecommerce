package com.study.ecommerce.domain.cart.dto.req;

import jakarta.validation.constraints.NotNull;

public record CartItemRequest(
        @NotNull(message = "상품 ID는 필수입니다")
        Long productId,

        @NotNull(message = "수량은 필수입니다.")
        @NotNull(message = "수량은 정수이어야 합니다.")
        Integer quantity
) {
}
