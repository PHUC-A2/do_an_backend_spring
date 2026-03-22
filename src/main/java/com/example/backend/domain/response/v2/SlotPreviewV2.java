package com.example.backend.domain.response.v2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Một tiết trong preview lịch (số thứ tự + khoảng giờ).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotPreviewV2 {
    private int slotNumber;
    private String startTime;
    private String endTime;
}
