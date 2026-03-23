package com.example.backend.domain.response.device;

import java.time.Instant;

import com.example.backend.util.constant.device.DeviceStatus;
import com.example.backend.util.constant.device.DeviceType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResDeviceDetailDTO {

    private Long id;
    private Long assetId;
    private String assetName;
    private String deviceName;
    private Integer quantity;
    private DeviceStatus status;
    private DeviceType deviceType;
    /** Tên file ảnh minh họa thiết bị (tùy chọn). */
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
