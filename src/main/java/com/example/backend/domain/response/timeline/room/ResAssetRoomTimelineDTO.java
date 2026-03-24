package com.example.backend.domain.response.timeline.room;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Timeline phòng tin (asset) theo ngày — PERIODS: 10 tiết; FLEXIBLE: khoảng bận + cửa sổ xem.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResAssetRoomTimelineDTO {
    private LocalDate date;
    private Long assetId;
    /** PERIODS | FLEXIBLE */
    private String mode;
    private LocalTime openTime;
    private LocalTime closeTime;
    private int slotMinutes;
    private List<ResRoomPeriodSlotDTO> periods;
    private List<ResRoomFlexibleSlotDTO> slots;
    private LocalTime flexibleViewStart;
    private LocalTime flexibleViewEnd;
    private List<ResRoomBusyIntervalDTO> busyIntervals;
}
