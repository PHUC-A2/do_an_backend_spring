package com.example.backend.util.schedule;

import java.time.LocalTime;
import java.util.List;

/**
 * 10 tiết / ngày theo biểu mẫu trường: mỗi tiết 50 phút, nghỉ xen kẽ 5–10 phút.
 * Sáng: tiết 1–5; chiều: tiết 6–10 (13:00–17:35).
 */
public final class RoomPeriodDefinitions {

    public record PeriodDef(int index, LocalTime start, LocalTime end) {
    }

    /** Khung giờ chuẩn — đồng bộ với bảng tiết (SÁNG / CHIỀU). */
    public static final List<PeriodDef> TEN_PERIODS = List.of(
            new PeriodDef(1, LocalTime.of(7, 0), LocalTime.of(7, 50)),
            new PeriodDef(2, LocalTime.of(7, 55), LocalTime.of(8, 45)),
            new PeriodDef(3, LocalTime.of(8, 55), LocalTime.of(9, 45)),
            new PeriodDef(4, LocalTime.of(9, 50), LocalTime.of(10, 40)),
            new PeriodDef(5, LocalTime.of(10, 45), LocalTime.of(11, 35)),
            new PeriodDef(6, LocalTime.of(13, 0), LocalTime.of(13, 50)),
            new PeriodDef(7, LocalTime.of(13, 55), LocalTime.of(14, 45)),
            new PeriodDef(8, LocalTime.of(14, 55), LocalTime.of(15, 45)),
            new PeriodDef(9, LocalTime.of(15, 50), LocalTime.of(16, 40)),
            new PeriodDef(10, LocalTime.of(16, 45), LocalTime.of(17, 35)));

    private RoomPeriodDefinitions() {
    }
}
