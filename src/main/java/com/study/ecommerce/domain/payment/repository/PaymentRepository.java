package com.study.ecommerce.domain.payment.repository;

import com.study.ecommerce.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("select p from Payment p where p.id = :id")
    Optional<Payment> findByOrderId(@Param("id") Long id);

    Optional<Payment> findByPaidAtBetween(LocalTime startTime, LocalTime endTime);
}
