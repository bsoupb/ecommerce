package com.study.ecommerce.infra.shipping.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * CJ대한통운 외부 API (실제 API 호출 시뮬레이션)
 */
@Slf4j
@Component
public class CjShippingApi {

//    private final Map<String, CjShippingResponse> result = new HashMap<>();

    /**
     * CJ대한통운 배송 등록
     */
    public CjShippingResponse registerDelivery(CjShippingRequest request) {
        // TODO
//        log.info("CJ대한통운 API 호출: {}", request);
//
//
//        // 운송장 번호
//        String invoiceNo = UUID.randomUUID().toString();
//
//        // resultCode
//        boolean isValidDelivery = false;
//        String resultCode = !isValidDelivery ? "0000" : "기타";
//
//        // resultMessage
//        String message = !isValidDelivery ? "배송완료" : "배송실패";
//
//        CjShippingResponse resp = CjShippingResponse.builder()
//                .resultCode(resultCode)
//                .resultMessage(message)
//                .invoiceNo(invoiceNo)
//                .orderNo(request.orderNo())
//                .deliveryCharge(calculateDeliveryCharge(request))
//                .build();
//
//        result.put(invoiceNo, resp);
//
//        return resp;
        
        String trackingNumber = "CJ" + System.currentTimeMillis();
        
        return CjShippingResponse.builder()
                .resultCode("0000")
                .resultMessage("성공")
                .invoiceNo(trackingNumber)
                .orderNo(request.orderNo())
                .deliveryCharge(calculateDeliveryCharge(request))
                .build();
    }

    /**
     * 배송 상태 조회
     */
    public CjTrackingResponse getTrackingInfo(String invoiceNo) {
        // TODO
        return CjTrackingResponse.builder()
                .resultCode("0000")
                .resultMessage("성공")
                .invoiceNo(invoiceNo)
                .deliveryStatus("30")
                .deliveryStatusName("배송중")
                .build();
    }

    /**
     * 배송 취소
     */
    public CjShippingResponse cancelDelivery(String invoiceNo, String cancelReason) {
        // TODO
//        CjShippingResponse response = result.get(invoiceNo);
//
//        if(response == null) {
//            return CjShippingResponse.builder()
//                    .resultCode("기타")
//                    .resultMessage("해당 택배의 배송 정보를 찾을 수 없습니다.")
//                    .invoiceNo(invoiceNo)
//                    .build();
//        }
//
//        return CjShippingResponse.builder()
//                .resultCode("0000")
//                .resultMessage(cancelReason)
//                .invoiceNo(invoiceNo)
//                .orderNo(response.orderNo())
//                .deliveryCharge(response.deliveryCharge())
//                .build();
        log.info("CJ 대한통운 API 호출 - 배송 취소 {} - {}", invoiceNo, cancelReason);

        return CjShippingResponse.builder()
                .resultCode("0000")
                .resultMessage("취소 완료")
                .invoiceNo(invoiceNo)
                .build();
    }

    /**
     * 배송비 계산 (CJ 고유 로직)
     */
    public int calculateDeliveryCharge(CjShippingRequest request) {
        int baseCharge = 3000; // 기본 배송비

        // 무게에 따른 추가 요금
//        int weight = request.weight();
//
//        if(weight <= 2000) {
//            baseCharge += 2000;
//        } else if(weight <= 5000) {
//            baseCharge += 3000;
//        } else {
//            baseCharge += 5000;
//        }
//        // 5kg 초과
//
//        // 제주도/도서산간 추가 요금
//        String addr = request.receiverAddr();
//
//        if(addr != null && addr.contains("제주")) {
//            baseCharge += 4000;
//        }
//
//        return baseCharge;

        // 5kg 초과
        if(request.weight() > 5000) {
            baseCharge += 2000;
        }

        // 제주도/도서산간 추가 요금
        if(request.receiverZipCode().startsWith("63")) {
            baseCharge += 3000;
        }

        return baseCharge;
    }

//    enum DeliveryStatusEnum {
//        RECEIVED("10", "접수"),
//        PICKED_UP("20", "집하"),
//        DELIVERED("30", "배송중"),
//        COMPLETED("40", "배송완료");
//
//        private final String deliveryStatus;
//        private final String deliveryStatusName;
//
//        DeliveryStatusEnum(String deliveryStatus, String deliveryStatusName) {
//            this.deliveryStatus = deliveryStatus;
//            this.deliveryStatusName = deliveryStatusName;
//        }
//
//        public String getDeliveryStatus() {
//            return deliveryStatus;
//        }
//
//        public String getDeliveryStatusName() {
//            return deliveryStatusName;
//        }
//
//        public static String findDeliveryStatus() {
//            return Arrays.stream(values())
//                    .map(DeliveryStatusEnum::getDeliveryStatus)
//                    .findFirst()
//                    .orElse(null);
//
//        }
//
//        public static String findDeliveryStatusName(String code) {
//            return Arrays.stream(values())
//                    .map(DeliveryStatusEnum::getDeliveryStatusName)
//                    .filter(c -> c.equals(code))
//                    .findFirst()
//                    .orElse(null);
//
//        }
//    }

}
