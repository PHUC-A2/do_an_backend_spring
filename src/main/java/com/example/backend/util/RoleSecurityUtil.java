package com.example.backend.util;

import com.example.backend.domain.entity.Role;

/**
 * Phân biệt role toàn hệ thống (không thuộc tenant) với role thuộc shop.
 * Chỉ role ADMIN toàn hệ thống mới ánh xạ sang quyền {@code ALL} trong JWT / UserDetails.
 */
public final class RoleSecurityUtil {

    public static final String SYSTEM_ADMIN_NAME = "ADMIN";
    public static final String SYSTEM_DEFAULT_VIEW_NAME = "VIEW";

    private RoleSecurityUtil() {
    }

    /** ADMIN + tenant gắn = toàn hệ thống (một bản ghi). */
    public static boolean isGlobalSystemAllRole(Role r) {
        if (r == null) {
            return false;
        }
        return SYSTEM_ADMIN_NAME.equals(r.getName()) && r.getTenant() == null;
    }

    public static boolean isGlobalSystemViewRole(Role r) {
        if (r == null) {
            return false;
        }
        return SYSTEM_DEFAULT_VIEW_NAME.equals(r.getName()) && r.getTenant() == null;
    }
}
