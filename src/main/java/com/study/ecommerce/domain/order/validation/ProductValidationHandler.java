package com.study.ecommerce.domain.order.validation;

import com.study.ecommerce.domain.order.dto.req.OrderCreateRequest;
import com.study.ecommerce.domain.order.dto.req.OrderCreateRequest.OrderItemRequest;
import com.study.ecommerce.domain.order.repository.OrderItemRepository;
import com.study.ecommerce.domain.product.entity.Product;
import com.study.ecommerce.domain.product.repository.ProductRepository;
import com.study.ecommerce.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductValidationHandler extends OrderValidationHandler {
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    // static 변수
    private static final int MAXIMUM_QUANTITY = 100;

    @Override
    protected void doValidate(OrderCreateRequest request) {
        for(OrderItemRequest orderItem : request.items()) {
            validateOrderItem(orderItem);
        }    
    }

    private void validateOrderItem(OrderItemRequest orderItem) {
        // 상품 id 검증 -> DB 검증
        if(orderItem.getProductId() == null || orderItem.getProductId() < 0) {
            fail("상품 ID가 올바르지 않습니다.");
        }

        // 수량 검증 -> DB 검증
        if(orderItem.getQuantity() <= 0) {
            fail("해당 상품의 재고가 없습니다.");
        }

        // 최대 주문 수량 검증
        if(orderItem.getQuantity() > MAXIMUM_QUANTITY) {
            fail("해당 상품은 100개까지 구매 가능합니다.");
        }

        // 상품 가격 검증 -> BigDecimal
        Product product = productRepository.findById(orderItem.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않은 상품입니다."));

        if(orderItem.getPrice().compareTo(BigDecimal.valueOf(product.getPrice())) != 0) {
            fail("상품 가격이 올바르지 않습니다.");
        }

        // 상품 존재 여부 검증 (실제로는 DB 참조)
        boolean productsExist = orderItemRepository.existsByProductId(orderItem.getProductId());
        if(!productsExist) {
            fail("존재하지 않은 상품입니다.");
        }

        // 상품 판매 가능 여부 검증 -> -> DB 검증
        boolean isSellable = orderItemRepository.existsByIdAndStockLessThan(orderItem.getProductId(), orderItem.getQuantity());
        if(!isSellable) {
            fail("재고 부족으로 인해 상품을 판매할 수 없습니다.");
        }
    }

    @Override
    protected String getHandlerName() {
        return "상품 검증 핸들러";
    }
}
