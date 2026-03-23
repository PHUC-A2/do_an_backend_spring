package com.example.backend.util.constant.assetusage;

/**
 * Trạng thái đăng ký sử dụng tài sản (bảng asset_usages) — tách tên miền khỏi đặt sân {@code Booking}.
 */
public enum AssetUsageStatus {
    PENDING,
    APPROVED,
    REJECTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
