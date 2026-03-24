package com.example.backend.domain.request.assetusage;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.backend.util.constant.asset.AssetRoomFeeMode;
import com.example.backend.util.constant.assetusage.AssetUsageStatus;
import com.example.backend.util.constant.assetusage.AssetUsageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateAssetUsageDTO {

    @NotNull(message = "Người dùng không được để trống")
    private Long userId;

    @NotNull(message = "Tài sản không được để trống")
    private Long assetId;

    @NotNull(message = "Loại (thuê/mượn) không được để trống")
    private AssetUsageType usageType;

    /** Null → backend lấy theo phòng hoặc FREE. */
    private AssetRoomFeeMode usageFeeMode;

    @NotNull(message = "Ngày không được để trống")
    private LocalDate date;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    @NotBlank(message = "Mục đích không được để trống")
    private String subject;

    /** Nếu null → PENDING khi tạo. */
    private AssetUsageStatus status;
}
