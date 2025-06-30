package com.study.ecommerce.infra.payment.gateway;

import com.study.ecommerce.infra.payment.dto.PaymentGatewayRequest;
import com.study.ecommerce.infra.payment.dto.PaymentGatewayResponse;
// gateway: 여러 개의 공통 인터페이스를 갖고 있는 역할

/*
    결제 게이트웨이 공통 인터페이스
 */
public interface PaymentGateway {

    PaymentGatewayResponse processPayment(PaymentGatewayRequest request);

    PaymentGatewayResponse getPaymentStatus(String transactionId);

    PaymentGatewayResponse cancelPayment(String transactionId, int cancelAmount);

    String getGatewayName();
}
