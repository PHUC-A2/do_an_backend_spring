package com.example.backend.domain.response.v2;

import java.time.Instant;

import com.example.backend.util.constant.v2.devicecatalog.DeviceCatalogStatusEnum;
import com.example.backend.util.constant.v2.devicecatalog.DeviceTypeEnum;
import com.example.backend.util.constant.v2.devicecatalog.MobilityTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResDeviceCatalogDTO {

    private Long id;
    private String deviceName;
    private DeviceTypeEnum deviceType;
    private MobilityTypeEnum mobilityType;
    private String description;
    private String imageUrl;
    private String manufacturer;
    private String model;
    private DeviceCatalogStatusEnum status;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
