package com.example.backend.domain.request.v2;

import com.example.backend.util.constant.v2.devicecatalog.DeviceCatalogStatusEnum;
import com.example.backend.util.constant.v2.devicecatalog.DeviceTypeEnum;
import com.example.backend.util.constant.v2.devicecatalog.MobilityTypeEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateDeviceCatalogDTO {

    @NotBlank(message = "Tên thiết bị không được để trống")
    @Size(max = 255)
    private String deviceName;

    @NotNull(message = "Loại thiết bị không được để trống")
    private DeviceTypeEnum deviceType;

    @NotNull(message = "Kiểu cố định/lưu động không được để trống")
    private MobilityTypeEnum mobilityType;

    private String description;

    private String imageUrl;

    @Size(max = 255)
    private String manufacturer;

    @Size(max = 255)
    private String model;

    @NotNull(message = "Trạng thái không được để trống")
    private DeviceCatalogStatusEnum status;
}
