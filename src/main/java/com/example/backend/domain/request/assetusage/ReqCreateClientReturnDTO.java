package com.example.backend.domain.request.assetusage;

import java.time.Instant;

import com.example.backend.util.constant.devicereturn.DeviceCondition;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateClientReturnDTO {
    private Instant returnTime;

    @NotNull(message = "Tình trạng thiết bị không được để trống")
    private DeviceCondition deviceStatus;

    /** Trả tốt (tổng) — phụ thuộc borrowDevicesJson (FE gửi). */
    private Integer quantityReturnedGood;

    /** Mất (tổng) — nếu > 0 thì bắt buộc ký xác nhận. */
    private Integer quantityLost;

    /** Hỏng (tổng) — nếu > 0 thì bắt buộc ký xác nhận. */
    private Integer quantityDamaged;

    /** Họ tên người mượn ký xác nhận khi có mất/hỏng. */
    private String borrowerSignName;

    /** Họ tên nhân viên / bên giao nhận ký xác nhận khi có mất/hỏng. */
    private String staffSignName;

    /** Snapshot tên người trả (ưu tiên input, fallback user). */
    private String returnerName;

    /** Snapshot SĐT người trả (ưu tiên input, fallback contactPhone). */
    private String returnerPhone;

    /** Họ tên người nhận thiết bị tại sân (bắt buộc). */
    private String receiverName;

    /** SĐT người nhận thiết bị tại sân (bắt buộc). */
    private String receiverPhone;

    /** Ghi chú biên bản khi trả phòng. */
    private String returnConditionNote;

    /** Tùy chọn in/lưu biên bản trả phòng. */
    private Boolean returnReportPrintOptIn;
}
