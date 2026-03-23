package com.example.backend.util.constant.device;

/**
 * Trạng thái thiết bị theo bảng devices (db.md) — tách biệt Equipment sân bóng.
 */
public enum DeviceStatus {
    AVAILABLE,
    IN_USE,
    BORROWED,
    MAINTENANCE,
    BROKEN,
    LOST
}
