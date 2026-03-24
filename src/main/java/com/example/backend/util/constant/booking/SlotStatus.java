package com.example.backend.util.constant.booking;

public enum SlotStatus {
    /** Khung giờ đã kết thúc (so với hiện tại), không còn đặt được. */
    PAST,
    /** Chưa có booking, còn có thể đặt (nếu chưa qua giờ). */
    FREE,
    /** Có yêu cầu đặt sân đang chờ admin duyệt. */
    PENDING,
    /** Khung giờ đã được admin duyệt / thanh toán (đã có người đặt). */
    BOOKED,
    /** Người xem đã từng chờ duyệt ở khung giờ này nhưng admin đã duyệt người khác. */
    BOOKED_BY_OTHER
}
