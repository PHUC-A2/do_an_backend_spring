package com.example.backend.domain.response.timeline;

import java.time.LocalDateTime;

import com.example.backend.util.constant.booking.SlotStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResPitchTimelineSlotDTO {
    private LocalDateTime start;
    private LocalDateTime end;
    private SlotStatus status; // FREE | BUSY
    /*
     * FREE, // chưa có ai đặt -> cho phép đặt
     * BUSY // đã có booking trùng → không cho đặt
     */
}
