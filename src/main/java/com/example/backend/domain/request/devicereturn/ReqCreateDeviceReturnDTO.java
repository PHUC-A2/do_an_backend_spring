package com.example.backend.domain.request.devicereturn;

import java.time.Instant;

import com.example.backend.util.constant.devicereturn.DeviceCondition;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateDeviceReturnDTO {

    @NotNull(message = "Phiếu checkout không được để trống")
    private Long checkoutId;

    private Instant returnTime;

    @NotNull(message = "Tình trạng sau khi dùng không được để trống")
    private DeviceCondition deviceStatus;
}
