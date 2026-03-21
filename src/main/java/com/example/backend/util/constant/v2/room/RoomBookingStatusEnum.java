package com.example.backend.util.constant.v2.room;

/**
 * Trạng thái vòng đời đơn đặt phòng tin học (bảng {@code room_bookings_v2}).
 */
public enum RoomBookingStatusEnum {

    /** Vừa tạo, chờ admin xử lý / duyệt. */
    PENDING,

    /** Đã duyệt, phòng được giữ theo khung giờ đặt. */
    APPROVED,

    /** Bị từ chối (kèm lý do). */
    REJECTED,

    /** Đã hoàn tất sử dụng (sau mượn/trả). */
    COMPLETED,

    /** Đã hủy (do người dùng hoặc quy trình). */
    CANCELLED
}
