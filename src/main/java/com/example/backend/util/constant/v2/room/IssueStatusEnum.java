package com.example.backend.util.constant.v2.room;

/**
 * Trạng thái xử lý báo cáo lỗi thiết bị (bảng {@code room_device_issues_v2}).
 */
public enum IssueStatusEnum {

    /** Mới báo, chưa ai nhận xử lý. */
    REPORTED,

    /** Đang được kỹ thuật / admin xử lý. */
    IN_PROGRESS,

    /** Đã xử lý xong (có thể kèm ghi chú cách xử lý). */
    RESOLVED,

    /** Đã đóng hồ sơ, không còn theo dõi. */
    CLOSED
}
