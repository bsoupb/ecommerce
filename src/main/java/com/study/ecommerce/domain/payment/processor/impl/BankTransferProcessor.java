package com.study.ecommerce.domain.payment.processor.impl;

import com.study.ecommerce.domain.payment.dto.PaymentRequest;
import com.study.ecommerce.domain.payment.dto.PaymentResult;
import com.study.ecommerce.domain.payment.processor.PaymentProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class BankTransferProcessor implements PaymentProcessor {
    private static final int MAX_AMOUNT = 10_000_000;
    private static final int FIXED_FEE = 500;

    @Override
    public PaymentResult process(PaymentRequest request) {
        log.info("계좌번호로 송금 시작 - 주문ID: {}, 금액: {}", request.orderId(), request.amount());

        // 계좌 유효성 검증
        if(!isValidAccount(request.accountNumber())) {
            return PaymentResult.builder()
                    .success(false)
                    .message("유효하지 않은 계좌번호 입니다.")
                    .paymentMethod("BANK_TRANSFER")
                    .build();
        }

        // 송금 한도 확인
        if(request.amount() > MAX_AMOUNT) {
            return PaymentResult.builder()
                    .success(false)
                    .message("송금 한도 금액을 초과했습니다.")
                    .paymentMethod("BANK_TRANSFER")
                    .build();
        }

        String transactionId = "BT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        int feeAmount = calculateFee(request.amount());

        return PaymentResult.builder()
                .success(true)
                .transactionId(transactionId)
                .message("계좌 결제가 완료되었습니다.")
                .feeAmount(feeAmount)
                .paymentMethod("BANK_TRANSFER")
                .build();
    }

    @Override
    public int calculateFee(int amount) {
        return FIXED_FEE;
    }

    @Override
    public boolean supports(String paymentMethod) {
        return "BANK_TRANSFER".equals(paymentMethod);
    }

    @Override
    public int getMaxAmount() {
        return MAX_AMOUNT;
    }

    // isValidAccount -> 10자리 이상 14자리 이하
    public boolean isValidAccount(String accountNumber) {
        return accountNumber != null && accountNumber.length() >= 10 && accountNumber.length() <= 14;
    }
}
