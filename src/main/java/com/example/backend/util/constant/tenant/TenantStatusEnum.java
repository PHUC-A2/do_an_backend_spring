package com.example.backend.util.constant.tenant;

/**
 * Trạng thái yêu cầu / hoạt động cửa hàng (tenant).
 */
public enum TenantStatusEnum {
    /** Chờ quản trị hệ thống duyệt */
    PENDING,
    /** Đã duyệt, chủ sân dùng dashboard */
    APPROVED,
    /** Từ chối yêu cầu */
    REJECTED
}
