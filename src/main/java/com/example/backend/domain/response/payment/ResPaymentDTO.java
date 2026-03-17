package com.example.backend.domain.response.payment;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.backend.util.constant.payment.PaymentMethodEnum;
import com.example.backend.util.constant.payment.PaymentStatusEnum;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPaymentDTO {

    private Long id;

    private Long bookingId;
    private String proofUrl;

    private String paymentCode;
    private BigDecimal amount;
    private String content;

    private PaymentStatusEnum status;
    private PaymentMethodEnum method;

    // Người thanh toán (từ booking.user)
    private Long userId;
    private String userName;
    private String userFullName;
    private String userEmail;
    private String userPhone;
    private String userAvatarUrl;

    // Thông tin booking
    private String pitchName;
    private String contactPhone;
    private Instant bookingStart;
    private Instant bookingEnd;

    private Instant paidAt;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}