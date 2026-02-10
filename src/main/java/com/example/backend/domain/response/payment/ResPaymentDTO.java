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
   
    private Instant paidAt;
    private Instant createdAt;
}