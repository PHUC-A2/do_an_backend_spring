package com.example.backend.domain.request.bookingequipment;

import com.example.backend.util.constant.equipment.EquipmentMobilityEnum;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateBookingEquipmentDTO {

    @NotNull(message = "bookingId không được để trống")
    private Long bookingId;

    @NotNull(message = "equipmentId không được để trống")
    private Long equipmentId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng mượn tối thiểu là 1")
    private Integer quantity;

    /** Chỉ hợp lệ khi thiết bị trên sân là MOVABLE (cho mượn). */
    @NotNull(message = "Vui lòng chọn loại thiết bị mượn (lưu động)")
    private EquipmentMobilityEnum equipmentMobility;

    /** Ghi chú biên bản lúc mượn (tình trạng, kiểm tra). */
    private String borrowConditionNote;

    /**
     * Bắt buộc {@code true} khi khách tự tạo mượn: đã xem/xác nhận tình trạng thiết bị (tránh khiếu nại sau).
     */
    private Boolean borrowConditionAcknowledged;

    /** Có muốn in hoặc lưu biên bản mượn (chữ ký chủ sân) hay không — chỉ ghi nhận khi {@code true}. */
    private Boolean borrowReportPrintOptIn;
}
