package com.study.ecommerce.domain.order.validation;

import com.study.ecommerce.domain.member.entity.Member;
import com.study.ecommerce.domain.member.repository.MemberRepository;
import com.study.ecommerce.domain.order.dto.req.OrderCreateRequest;
import com.study.ecommerce.domain.order.service.OrderService;
import com.study.ecommerce.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.study.ecommerce.domain.member.entity.Member.Role.CUSTOMER;

@Slf4j
@Component

@RequiredArgsConstructor
public class MemberValidationHandler extends OrderValidationHandler {
    private final MemberRepository memberRepository;
    private final OrderService orderService;
    private static final BigDecimal DAILY_ORDER_LIMIT = new BigDecimal("100");

    @Override
    protected void doValidate(OrderCreateRequest request) {
        Long memberId = request.memberId();

        // 1. 회원 존재 여부
        if(!isMemberExists(memberId)) {
            fail("존재하지 않은 회원입니다.");
        }

        // 2. 회원 활성화 상태 확인
        if(!isMemberActive(memberId)) {
            fail("비활성화 회원입니다.");
        }

        // 3. 주문 권한 존재 여부
        if(!hasOrderPermission(memberId)) {
            fail("주문 권한이 없습니다.");
        }

        // 4. 신용도 확인
        if(!isCreditWorthy(memberId)) {
            fail("신용도가 낮아 주문 권한이 없습니다.");
        }

        // 5. 일일 주문 한도
        if(!exceedsDailyOrderLimit(memberId, request)) {
            fail("일일 주문 한도 금액을 초과했습니다.");
        }
    }

    @Override
    protected String getHandlerName() {
        return "회원 정보 검증";
    }

    // 회원 존재 여부
    private boolean isMemberExists(Long memberId) {
        // memberService -> existsById(memberId)
        return memberRepository.existsById(memberId);
    }

    // 회원 활성화 상태 확인
    private boolean isMemberActive(Long memberId) {
        // isActive(memberId)

        // Member entity에 isDeleted 필드 추가
        return memberRepository.existsByIdAndIsDeletedFalse(memberId);
    }

    // 주문 권한 존재 여부
    private boolean hasOrderPermission(Long memberId) {
        // hasOrderPermission(memberId)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않은 회원입니다."));

        return member.getRole().equals(CUSTOMER);
    }

    // 신용도 확인
    private boolean isCreditWorthy(Long memberId) {
        // getCreditScore(memberId) >= MINIMUM_CREDIT_SCORE;
        log.info("신용도 확인 시작");
        log.info("신용도 확인 완료");

        return true;
    }

    // 일일 주문 한도
    private boolean exceedsDailyOrderLimit(Long memberId, OrderCreateRequest request) {
        // OrderService 에서 일일 주문내역 확인
         BigDecimal todayOrderAmount = orderService.getTodayOrderAmount(memberId);
         BigDecimal requestAmount = orderService.calculateTotalAmount(request);
         return todayOrderAmount.add(requestAmount).compareTo(DAILY_ORDER_LIMIT) > 0;
    }
}
