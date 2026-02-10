package com.example.backend.domain.response.payment;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPaymentQRDTO {

    private Long paymentId;

    private String paymentCode;

    private String bankCode; // VCB
    private String accountNo; // 0123456789
    private String accountName; // NGUYEN VAN A

    private BigDecimal amount;
    private String content; // BOOKING_10

    private String vietQrUrl; // FE chỉ <img src="">
}