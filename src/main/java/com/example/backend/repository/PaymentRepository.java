package com.example.backend.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Lấy payment theo bookingId + status (để phục hồi QR khi tải lại)
    Optional<Payment> findByBooking_IdAndStatus(Long bookingId, PaymentStatusEnum status);

     // ===== Tổng doanh thu theo khoảng thời gian =====
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.status = :status
        AND p.paidAt BETWEEN :start AND :end
    """)
    BigDecimal sumRevenueByDateRange(
            @Param("status") PaymentStatusEnum status,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    // ===== Doanh thu theo ngày =====
    @Query("""
        SELECT FUNCTION('DATE', p.paidAt), SUM(p.amount)
        FROM Payment p
        WHERE p.status = :status
        AND p.paidAt BETWEEN :start AND :end
        GROUP BY FUNCTION('DATE', p.paidAt)
        ORDER BY FUNCTION('DATE', p.paidAt)
    """)
    List<Object[]> revenueGroupedByDate(
            @Param("status") PaymentStatusEnum status,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    // ===== Doanh thu theo sân =====
    @Query("""
        SELECT p.booking.pitch.id,
               p.booking.pitch.name,
               SUM(p.amount)
        FROM Payment p
        WHERE p.status = :status
        AND p.paidAt BETWEEN :start AND :end
        GROUP BY p.booking.pitch.id, p.booking.pitch.name
    """)
    List<Object[]> revenueGroupedByPitch(
            @Param("status") PaymentStatusEnum status,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    long countByStatus(PaymentStatusEnum status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatusEnum status);

}