package com.example.backend.util.constant.v2.room;

/**
 * Tình trạng thiết bị cụ thể trong phòng (bảng {@code room_devices_v2}).
 */
public enum DeviceStatusEnum {

    /** Hoạt động tốt, sử dụng được. */
    GOOD,

    /** Hỏng, cần sửa hoặc thay thế. */
    BROKEN,

    /** Báo mất, không còn trong phòng. */
    LOST,

    /** Đang bảo trì, tạm không dùng. */
    MAINTENANCE
}
