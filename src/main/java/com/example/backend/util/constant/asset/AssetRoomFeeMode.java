package com.example.backend.util.constant.asset;

/**
 * Phân loại phí đặt phòng/tài sản do admin cấu hình — map cột {@code room_fee_mode} bảng {@code assets}.
 * FREE: miễn phí; PAID: có phí (mức cụ thể có thể bổ sung sau, hiện chỉ hiển thị nhãn).
 */
public enum AssetRoomFeeMode {
    FREE,
    PAID
}
