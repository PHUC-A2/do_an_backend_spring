package com.example.backend.domain.request.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Body tùy chọn cho PUT xác nhận thanh toán: PIN khi hệ thống bật chế độ bắt buộc.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqConfirmPaymentDTO {

    private String pin;
}
