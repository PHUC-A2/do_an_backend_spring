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
    /** PAST | FREE | PENDING | BOOKED | BOOKED_BY_OTHER */
    private SlotStatus status;
}
