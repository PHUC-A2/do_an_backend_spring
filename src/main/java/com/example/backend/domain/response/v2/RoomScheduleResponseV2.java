package com.example.backend.domain.response.v2;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Phản hồi cấu hình lịch tiết kèm preview các tiết.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomScheduleResponseV2 {

    private Long id;
    private Long roomId;

    private Integer totalSlots;
    private Integer slotDuration;
    private Integer breakDuration;

    /** Nghỉ sau từng tiết (sáng); null nếu dùng nghỉ đồng đều. */
    private List<Integer> morningGapBreaks;

    /** Nghỉ sau từng tiết (chiều); null nếu dùng nghỉ đồng đều. */
    private List<Integer> afternoonGapBreaks;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime morningStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime morningEnd;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime afternoonStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime afternoonEnd;

    private Boolean isActive;

    private Instant createdAt;
    private Instant updatedAt;

    private List<SlotPreviewV2> slots;
}
