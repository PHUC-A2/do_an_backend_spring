package com.example.backend.domain.request.payment;

import com.example.backend.util.constant.payment.PaymentMethodEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreatePaymentDTO {

    @NotNull
    private Long bookingId;

    @NotNull
    private PaymentMethodEnum method; // BANK_TRANSFER / CASH
}