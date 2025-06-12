package com.study.ecommerce.domain.order.repository;

import com.study.ecommerce.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByMemberId(Long id);
}
