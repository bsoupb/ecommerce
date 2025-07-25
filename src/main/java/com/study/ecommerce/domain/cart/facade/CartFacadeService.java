package com.study.ecommerce.domain.cart.facade;

import com.study.ecommerce.domain.cart.dto.req.CartItemRequest;
import com.study.ecommerce.domain.cart.dto.resp.CartItemResponse;
import com.study.ecommerce.domain.cart.dto.resp.CartResponse;

public interface CartFacadeService {

    CartResponse getCart(String email);

    CartItemResponse addItemToCart(String email, CartItemRequest request);

    CartItemResponse updateCartItemQuantity(String email, Long cartItemId, Integer quantity);

    void removeCartItem(String email, Long cartItemId);

    void clearCart(String email);
}
