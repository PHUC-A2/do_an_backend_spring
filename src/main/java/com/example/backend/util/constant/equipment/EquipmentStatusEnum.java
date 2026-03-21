package com.example.backend.util.constant.equipment;

public enum EquipmentStatusEnum {
    /** Hoạt động tốt, cho mượn được. */
    ACTIVE,
    /** Bảo trì / tạm ngưng cho mượn. */
    MAINTENANCE,
    /** Ngừng sử dụng trong hệ thống. */
    INACTIVE,
    /** Hỏng, không cho mượn. */
    BROKEN,
    /** Đã mất / thất lạc trong kho (không cho mượn). */
    LOST
}
