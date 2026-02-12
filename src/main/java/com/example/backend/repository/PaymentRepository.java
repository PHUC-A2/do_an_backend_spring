package com.example.backend.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.Payment;
import com.example.backend.util.constant.payment.PaymentStatusEnum;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    Optional<Payment> findByPaymentCode(String paymentCode);

    // Chặn tạo payment trùng
    boolean existsByBooking_IdAndStatusIn(
            Long bookingId,
            Collection<PaymentStatusEnum> statuses);

    List<Payment> findByStatus(PaymentStatusEnum status);

}