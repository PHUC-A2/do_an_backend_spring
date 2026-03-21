package com.example.backend.util.constant.v2.room;

/**
 * Loại bản ghi trong nhật ký mượn/trả phòng (bảng {@code room_borrow_logs_v2}).
 */
public enum LogTypeEnum {

    /** Ghi nhận thời điểm và biên bản mượn phòng / thiết bị. */
    BORROW,

    /** Ghi nhận thời điểm và biên bản trả phòng / thiết bị. */
    RETURN
}
