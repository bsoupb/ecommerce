package com.study.ecommerce.domain.payment.dto;

import lombok.Builder;

@Builder
public record PaymentResult(
        boolean success,
        String transactionId,       // 결제 처리 id
        String message,
        int paidAmount,             // 결제된 금액
        int feeAmount,              // 결제 수수료
        String paymentMethod
) {
}
