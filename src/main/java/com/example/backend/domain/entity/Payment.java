package com.example.backend.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.backend.util.SecurityUtil;
import com.example.backend.util.constant.payment.PaymentMethodEnum;
import com.example.backend.util.constant.payment.PaymentStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

   @Column(length = 255)
    private String content; // BOOKING_10 / nội dung thanh toán

    @Column(nullable = false, unique = true)
    private String paymentCode; // VD: PAY_20260210_0001/ là mã giao dịch

    /**
     * PENDING, // đã tạo, chưa thanh toán
     * PAID, // đã nhận tiền
     * CANCELLED // hủy / không thanh toán
     */
    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum status = PaymentStatusEnum.PENDING;

    /*
     * BANK_TRANSFER, // QR / chuyển khoản
     * CASH // tiền mặt
     */
    @Enumerated(EnumType.STRING)
    private PaymentMethodEnum method = PaymentMethodEnum.BANK_TRANSFER;

    private Instant paidAt; // thời điểm thanh toán thành công

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdAt = Instant.now(); // tạo ra lúc
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.updatedAt = Instant.now(); // cập nhật lúc
    }
}
