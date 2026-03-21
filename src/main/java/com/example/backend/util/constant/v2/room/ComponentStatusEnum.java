package com.example.backend.util.constant.v2.room;

/**
 * Trạng thái linh kiện thuộc một thiết bị (bảng {@code room_device_components_v2}).
 */
public enum ComponentStatusEnum {

    /** Linh kiện hoạt động bình thường. */
    GOOD,

    /** Linh kiện hỏng. */
    BROKEN,

    /** Đã thay thế bằng linh kiện khác. */
    REPLACED
}
