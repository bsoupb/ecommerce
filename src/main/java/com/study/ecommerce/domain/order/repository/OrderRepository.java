package com.study.ecommerce.domain.order.repository;

import com.study.ecommerce.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByMemberId(Long id, Pageable pageable);

    Optional<Order> findByMemberId(Long id);

    List<Order> findByOrderDate(LocalDate orderDate);
}
