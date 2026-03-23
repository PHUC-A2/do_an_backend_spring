package com.example.backend.domain.request.checkout;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateCheckoutDTO {

    @NotNull(message = "Đăng ký sử dụng tài sản không được để trống")
    private Long assetUsageId;

    /** Null → lấy thời điểm hiện tại khi nhận. */
    private Instant receiveTime;

    private String conditionNote;
}
