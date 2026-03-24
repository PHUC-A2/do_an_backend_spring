package com.example.backend.domain.response.timeline.room;

import java.time.LocalDateTime;

import com.example.backend.util.constant.assetusage.AssetUsageStatus;
import com.example.backend.util.constant.booking.SlotStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Slot 5 phút cho chế độ đặt giờ linh hoạt của phòng. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResRoomFlexibleSlotDTO {
    private LocalDateTime start;
    private LocalDateTime end;
    private SlotStatus status;
    private AssetUsageStatus busyUsageStatus;
}
