package com.example.backend.util.constant.booking;

public enum SlotStatus {
    /** Khung giờ đã kết thúc (so với hiện tại), không còn đặt được. */
    PAST,
    /** Chưa có booking, còn có thể đặt (nếu chưa qua giờ). */
    FREE,
    /** Đã có booking chiếm khung giờ. */
    BUSY
}
