package com.example.backend.util.constant.device;

/**
 * Trạng thái thiết bị theo bảng devices (db.md) — tách biệt Equipment sân bóng.
 */
public enum DeviceStatus {
    AVAILABLE, // Sẵn sàng
    IN_USE, // Đang sử dụng
    BORROWED, // Đang mượn
    MAINTENANCE, // Bảo trì
    BROKEN, // Hỏng
    LOST // Mất
}
