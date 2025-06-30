package com.study.ecommerce.domain.payment.dto;

public record PaymentRequest(
        String orderId,
        int amount,     // 금액
        String paymentMethod,
        String cardNumber,
        String accountNumber,
        String simplePayProvider,
        String customerId
) {
}
