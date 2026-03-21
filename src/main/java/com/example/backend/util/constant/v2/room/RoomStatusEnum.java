package com.example.backend.util.constant.v2.room;

/**
 * Trạng thái vận hành của phòng tin học (bảng {@code rooms_v2}).
 */
public enum RoomStatusEnum {

    /** Phòng đang sử dụng bình thường, có thể mở cho đặt lịch (nếu không có ràng buộc khác). */
    ACTIVE,

    /** Phòng tạm ngưng sử dụng, không nhận đặt lịch mới. */
    INACTIVE,

    /** Phòng đang bảo trì, không cho đặt cho đến khi hết bảo trì. */
    MAINTENANCE
}
