package com.example.backend.domain.response.assetusage;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import com.example.backend.util.constant.asset.AssetRoomFeeMode;
import com.example.backend.util.constant.assetusage.AssetUsageStatus;
import com.example.backend.util.constant.assetusage.AssetUsageType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResAssetUsageDetailDTO {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long assetId;
    private String assetName;
    private AssetUsageType usageType;
    /** Miễn phí / có phí ghi trên bản ghi đăng ký (AssetUsage). */
    private AssetRoomFeeMode usageFeeMode;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String subject;
    private String contactPhone;
    private String bookingNote;
    private String borrowDevicesJson;
    private String borrowNote;
    private Boolean borrowConditionAcknowledged;
    private Boolean borrowReportPrintOptIn;
    private AssetUsageStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private String assetResponsibleName;
    /** Ảnh minh họa phòng/tài sản. */
    private String assetAssetsUrl;

    /** Miễn phí / có phí theo cấu hình phòng (Asset). */
    private AssetRoomFeeMode assetRoomFeeMode;
}
