package com.example.backend.domain.response.timeline;

import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResPitchTimelineDTO {
    private LocalTime openTime;
    private LocalTime closeTime;
    private int slotMinutes;
    private List<ResPitchTimelineSlotDTO> slots;
}
