package com.example.backend.domain.response.timeline.room;

import java.time.LocalDateTime;

import com.example.backend.util.constant.assetusage.AssetUsageStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Khung đã có đăng ký sử dụng tài sản (phòng) — chế độ giờ linh hoạt. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResRoomBusyIntervalDTO {
    private LocalDateTime start;
    private LocalDateTime end;
    private AssetUsageStatus status;
}
