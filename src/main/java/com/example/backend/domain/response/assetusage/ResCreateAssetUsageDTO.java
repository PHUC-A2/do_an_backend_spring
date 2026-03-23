package com.example.backend.domain.response.assetusage;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import com.example.backend.util.constant.assetusage.AssetUsageStatus;
import com.example.backend.util.constant.assetusage.AssetUsageType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResCreateAssetUsageDTO {

    private Long id;
    private Long userId;
    private Long assetId;
    private AssetUsageType usageType;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String subject;
    private AssetUsageStatus status;
    private Instant createdAt;
}
