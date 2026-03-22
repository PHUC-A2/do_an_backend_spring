package com.example.backend.domain.request.v2;

import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body tạo cấu hình lịch tiết (POST {@code /api/v2/admin/rooms/{roomId}/schedules}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomScheduleRequestV2 {

    @NotNull(message = "Tổng số tiết không được để trống")
    @Min(value = 1, message = "Tổng số tiết từ 1 đến 20")
    @Max(value = 20, message = "Tổng số tiết từ 1 đến 20")
    private Integer totalSlots;

    @NotNull(message = "Thời lượng mỗi tiết không được để trống")
    @Min(value = 1, message = "Thời lượng mỗi tiết phải lớn hơn 0")
    private Integer slotDuration;

    @NotNull(message = "Nghỉ giữa tiết không được để trống")
    @Min(value = 0, message = "Nghỉ giữa tiết không được âm")
    private Integer breakDuration;

    @NotNull(message = "Giờ bắt đầu buổi sáng không được để trống")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime morningStart;

    @NotNull(message = "Giờ kết thúc buổi sáng không được để trống")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime morningEnd;

    @NotNull(message = "Giờ bắt đầu buổi chiều không được để trống")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime afternoonStart;

    @NotNull(message = "Giờ kết thúc buổi chiều không được để trống")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime afternoonEnd;

    /**
     * Phút nghỉ sau từng tiết buổi sáng (tùy chọn). Ví dụ 5 tiết → tối đa 4 phần tử [5,10,5,5].
     */
    private List<Integer> morningGapBreaks;

    /** Phút nghỉ sau từng tiết buổi chiều (tùy chọn). */
    private List<Integer> afternoonGapBreaks;
}
