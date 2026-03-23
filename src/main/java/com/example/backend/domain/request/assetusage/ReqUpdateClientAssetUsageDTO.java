package com.example.backend.domain.request.assetusage;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload cập nhật đặt phòng của chính user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqUpdateClientAssetUsageDTO {

    @NotNull(message = "Ngày không được để trống")
    private LocalDate date;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    @NotBlank(message = "Mục đích không được để trống")
    private String subject;

    /** SĐT liên hệ của user (tùy chọn). */
    private String contactPhone;

    /** Ghi chú booking (tùy chọn). */
    private String bookingNote;

    /** JSON thiết bị user chọn mượn kèm theo booking (tùy chọn). */
    private String borrowDevicesJson;

    /** Ghi chú chung tình trạng thiết bị (tùy chọn). */
    private String borrowNote;

    /** Xác nhận đã kiểm tra tình trạng thiết bị trước khi gửi yêu cầu. */
    private Boolean borrowConditionAcknowledged;

    /** Tùy chọn in/lưu biên bản nhận phòng. */
    private Boolean borrowReportPrintOptIn;
}
