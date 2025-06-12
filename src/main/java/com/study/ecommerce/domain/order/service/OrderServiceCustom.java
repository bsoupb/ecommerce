package com.study.ecommerce.domain.order.service;

import com.study.ecommerce.domain.cart.entity.Cart;
import com.study.ecommerce.domain.cart.entity.CartItem;
import com.study.ecommerce.domain.cart.repository.CartItemRepository;
import com.study.ecommerce.domain.cart.repository.CartRepository;
import com.study.ecommerce.domain.member.entity.Member;
import com.study.ecommerce.domain.member.repository.MemberRepository;
import com.study.ecommerce.domain.order.dto.OrderItemDto;
import com.study.ecommerce.domain.order.dto.req.OrderCreateRequest;
import com.study.ecommerce.domain.order.dto.req.OrderCreateRequest.OrderItemRequest;
import com.study.ecommerce.domain.order.dto.resp.OrderDetailResponse;
import com.study.ecommerce.domain.order.dto.resp.OrderResponse;
import com.study.ecommerce.domain.order.entity.Order;
import com.study.ecommerce.domain.order.entity.Order.OrderStatus;
import com.study.ecommerce.domain.order.entity.OrderItem;
import com.study.ecommerce.domain.order.repository.OrderItemRepository;
import com.study.ecommerce.domain.order.repository.OrderRepository;
import com.study.ecommerce.domain.payment.entity.Payment;
import com.study.ecommerce.domain.payment.repository.PaymentRepository;
import com.study.ecommerce.domain.product.entity.Product;
import com.study.ecommerce.domain.product.repository.ProductRepository;
import com.study.ecommerce.global.error.exception.EntityNotFoundException;
import com.study.ecommerce.infra.payment.service.MockPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.study.ecommerce.domain.order.entity.Order.OrderStatus.CANCELED;
import static com.study.ecommerce.domain.order.entity.Order.OrderStatus.CREATED;
import static com.study.ecommerce.domain.payment.entity.Payment.*;
import static com.study.ecommerce.domain.payment.entity.Payment.PaymentStatus.*;

@Service
@RequiredArgsConstructor
public class OrderServiceCustom implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentRepository paymentRepository;
    private final MockPaymentService mockPaymentService;

    // 주문서 생성
    // 1. 빈 주문서 만들기
    // 2. save
    // 3.
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)    // commit이 된 것만 읽어오겠다 
    public OrderResponse createOrder(OrderCreateRequest request, String email) {
        // 1. 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 2. 주문 생성(초기 총액 자체를 0원으로 설정)
        Order order = Order.builder()
                .memberId(member.getId())
                .status(CREATED)
                .orderDate(LocalDateTime.now())
                .totalAmount(0L)
                .build();

        order = orderRepository.save(order);

        // 주문서 생성한 뒤 총액 업데이트
        // 3. 주문 상품 처리 및 총액 계산
        long totalAmount = 0L;

        if(request.cartItemIds() != null && !request.cartItemIds().isEmpty()) {
            // 장바구니로 상품을 주문
            totalAmount = processCartItems(order, request.cartItemIds(), member);
        } else if(request.items() != null && !request.items().isEmpty()) {
            // 직접 지정한 상품 (장바구니에 있는건 아님)
            totalAmount = processDirectItems(order, request.items());
        } else {
            throw new IllegalArgumentException("주문한 상품이 지정되지 않았습니다.");
        }

        // 4. 총액 업데이트
        order.updateTotalAmount(totalAmount);
        order = orderRepository.save(order);

        // 5. 결제 진행 (비동기로 처리해도 됨)
        if(request.payNow()) {
            Payment payment = mockPaymentService.processPayment(order, PaymentMethod.valueOf(request.paymentMethod()));
            order.updateStatus(OrderStatus.PAID);
            orderRepository.save(order);
        }

        return new OrderResponse(order.getId(), order.getStatus(), order.getTotalAmount());

    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, String email) {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다. id = " + orderId));
        // 주문자 확인
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 주문 상태 확인
        OrderStatus status = order.getStatus();

        // 결제 취소(결제가 완료된 경우)
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다."));

        if(payment.getStatus().equals(COMPLETED)) {
            payment.updateStatus(PaymentStatus.CANCELED);
        } else {
            throw new IllegalArgumentException("결제 취소를 할 수 없습니다.");
        }

        // 결제 금액 원복
        Long totalAmount = payment.getAmount();

        // 재고 원복
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        Integer quantity = 0;
        for(OrderItem item : orderItems) {
            Product product = productRepository.findByIdWithPessimisticLock(item.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
            quantity = product.getStockQuantity() + item.getQuantity();
        }

        // 주문상태 변경
        order.updateStatus(OrderStatus.CANCELED);

        return new OrderResponse(orderId, order.getStatus(), totalAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long orderId, String email) {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다. id = " + orderId));

        // 주문자 확인
        Member member = memberRepository.findById(order.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        if(!member.getEmail().equals(email)) {
            throw new IllegalArgumentException("주문 조회 권한이 없습니다.");
        }

        // 주문 상품 조회
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemDto> orderItemDtos = orderItems.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

                    return new OrderItemDto(
                            product.getId(),
                            product.getName(),
                            item.getQuantity(),
                            item.getPrice()
                    );
                })
                .toList();

        return new OrderDetailResponse(
                order.getId(),
                member.getId(),
                member.getName(),
                order.getStatus(),
                order.getOrderDate(),
                order.getTotalAmount(),
                orderItemDtos
        );
    }

    @Override
    public Page<OrderResponse> getOrders(String email, Pageable pageable) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        if(!member.getEmail().equals(email)) {
            throw new IllegalArgumentException("주문 조회 권한이 없습니다.");
        }

        Order order = orderRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        Page<Order> page = orderRepository.findAll(pageable);

        return page
                .map(o -> new OrderResponse(
                        o.getId(),
                        o.getStatus(),
                        o.getTotalAmount()
                ));
    }

    // 전체 가격을 계산하기 위한 메서드
    private long processCartItems(Order order, List<Long> cartItemIds, Member member) {
        Cart cart = cartRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new EntityNotFoundException("장바구니를 찾을 수 없습니다."));

        long totalAmount = 0L;

        for(Long cartItemId : cartItemIds) {
            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new EntityNotFoundException("장바구니 상품을 찾을 수 없습니다." + cartItemIds));

            // 해당 장바구니 상품이 현재 사용자의 것인지
            if(!cartItem.getCartId().equals(cart.getId())) {
                throw new IllegalArgumentException("장바구니 상품 접근 권한이 없습니다.");
            }

            // 비관적락 사용
            Product product = productRepository.findByIdWithPessimisticLock(cartItem.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

            product.decreasesStock(cartItem.getQuantity());

            productRepository.save(product);

            // 주문 상품 추가
            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .productId(product.getId())
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();

            orderItemRepository.save(orderItem);
            totalAmount += orderItem.getTotalPrice();

            // 주문한 상품은 장바구니에서 제거
            cartItemRepository.delete(cartItem);
        }

        return totalAmount;
    }

    private long processDirectItems(Order order, List<OrderItemRequest> items) {
        long totalAmount = 0L;

        for(OrderItemRequest request : items) {
            // 상품 재고 확인 및 감소
            Product product = productRepository.findByIdWithPessimisticLock(request.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

            product.decreasesStock(request.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .productId(product.getId())
                    .quantity(request.getQuantity())
                    .price(product.getPrice())
                    .build();

            orderItemRepository.save(orderItem);
            totalAmount += orderItem.getTotalPrice();
        }
        return totalAmount;
    }

}
