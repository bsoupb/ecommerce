package com.study.ecommerce.domain.order.validation;

import com.study.ecommerce.domain.order.dto.req.OrderCreateRequest;
import com.study.ecommerce.domain.payment.entity.Payment;
import com.study.ecommerce.domain.payment.repository.PaymentRepository;
import com.study.ecommerce.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentValidationHandler extends OrderValidationHandler {
    private final PaymentRepository paymentRepository;

    private static final BigDecimal MINIMUM_ORDER_AMOUNT = new BigDecimal("1000");
    private static final BigDecimal MAXIMUM_ORDER_AMOUNT = new BigDecimal("1000000");
    private static final BigDecimal CURRENT_POINT = new BigDecimal("50000");
    private static final LocalTime START_TIME = LocalTime.of(12, 0);
    private static final LocalTime END_TIME = LocalTime.of(1, 0);


    @Override
    protected void doValidate(OrderCreateRequest request) {
        BigDecimal totalPrice = request.items().stream()
                .map(req ->
                        req.getPrice().multiply(BigDecimal.valueOf(req.getQuantity().longValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 결제 방법 검증
        // 지원하는 결제 방법인지 검증
        validatePaymentMethodSpecified(request, totalPrice);

        // 주문 총액 계산
        log.info("주문 총액: {}원", totalPrice);

        // 최소 주문 금액 검증
        if(totalPrice.compareTo(MINIMUM_ORDER_AMOUNT) < 0) {
            fail("최소 주문 금액에 맞게 주문해 주십시오.");
        }

        // 결제 방법별 추가 검증
        validatePointPayment(request, totalPrice);
        validateVirtualAccountPayment(request, totalPrice);
        validateBankTransferPayment(request, totalPrice);
        validateCardPayment(request, totalPrice);

    }

    @Override
    protected String getHandlerName() {
        return "결제 검증 핸들러";
    }

    private void validatePaymentMethodSpecified(OrderCreateRequest request, BigDecimal totalAmount) {
        String paymentMethod = request.paymentMethod();

        switch (paymentMethod) {
            case "CARD" -> validateCardPayment(request, totalAmount);
            case "BANK_TRANSFER" -> validateBankTransferPayment(request, totalAmount);
            case "VIRTUAL_ACCOUNT" -> validateVirtualAccountPayment(request, totalAmount);
            case "POINT" -> validatePointPayment(request, totalAmount);
        }
    }

    private void validatePointPayment(OrderCreateRequest request, BigDecimal totalAmount) {
        // 포인트 compareTo() 5만포인트
//        BigDecimal totalPrice = request.items().stream()
//                .map(req ->
//                        req.getPrice().multiply(BigDecimal.valueOf(req.getQuantity().longValue())))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        if(totalPrice.compareTo(totalAmount) < 0) {
//            fail("포인트가 부족합니다.");
//        }

        if(totalAmount.compareTo(CURRENT_POINT) < 0) {
            fail("포인트가 부족합니다.");
        }
    }

    private void validateVirtualAccountPayment(OrderCreateRequest request, BigDecimal totalAmount) {
        // 로그
        log.info("가상 계좌 결제 시작");
        log.info("가상 계좌 결제 완료");
    }

    private void validateBankTransferPayment(OrderCreateRequest request, BigDecimal totalAmount) {
        // 영업 시간에만 가능
//        Payment payment = paymentRepository.findByPaidAtBetween(START_TIME, END_TIME)
//                .orElseThrow(() -> new EntityNotFoundException("조건을 만족하지 않는 결제 내역입니다."));

        LocalTime now = LocalTime.now();

        if(!(now.isBefore(START_TIME) || now.isAfter(END_TIME))) {
            fail("현재 점검 시간 입니다. 다시 결제 해주세요.");
        }
    }

    private void validateCardPayment(OrderCreateRequest request, BigDecimal totalAmount) {
        // 최소금액 검증
        if(totalAmount.compareTo(MINIMUM_ORDER_AMOUNT) < 0) {
            fail("최소금액을 확인해 주십시오.");
        }
    }
}
