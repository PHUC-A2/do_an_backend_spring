package com.example.backend.domain.response.timeline.room;

import java.time.LocalDateTime;

import com.example.backend.util.constant.booking.SlotStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Một tiết trong ngày — trạng thái TRỐNG / BẬN / ĐÃ QUA. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResRoomPeriodSlotDTO {
    private int periodIndex;
    private String label;
    private LocalDateTime start;
    private LocalDateTime end;
    private SlotStatus status;
}
