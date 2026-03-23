package com.example.backend.domain.request.device;

import com.example.backend.util.constant.device.DeviceStatus;
import com.example.backend.util.constant.device.DeviceType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateDeviceDTO {

    @NotNull(message = "Tài sản không được để trống")
    private Long assetId;

    @NotBlank(message = "Tên thiết bị không được để trống")
    private String deviceName;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải ≥ 0")
    private Integer quantity;

    @NotNull(message = "Trạng thái không được để trống")
    private DeviceStatus status;

    @NotNull(message = "Loại thiết bị không được để trống")
    private DeviceType deviceType;
}
