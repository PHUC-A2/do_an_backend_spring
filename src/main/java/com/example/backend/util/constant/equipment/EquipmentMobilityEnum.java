package com.example.backend.util.constant.equipment;

/**
 * Phân loại thiết bị gắn với tài sản (sân): chỉ mô tả / nguyên trạng sân hay có luồng mượn–trả.
 */
public enum EquipmentMobilityEnum {
    /**
     * Thiết bị cố định trên sân (đèn, lưới, khung thành…): ghi thông số và mô tả cho khách xem,
     * không mượn qua booking (không trừ kho theo lượt đặt sân).
     */
    FIXED,
    /**
     * Thiết bị cho mượn thêm (bóng, áo…): hiển thị khi đặt sân, có luồng mượn / trả và biên bản.
     */
    MOVABLE
}
