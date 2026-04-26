package com.example.backend.tenant;

import java.util.Optional;

public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> BYPASS = ThreadLocal.withInitial(() -> false);

    private TenantContext() {
    }

    public static void setCurrentTenantId(Long tenantId) {
        CURRENT.set(tenantId);
    }

    public static Optional<Long> getCurrentTenantId() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static long requireCurrentTenantId() {
        return getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException("Thiếu tenant ngữ cảnh"));
    }

    public static void setBypassForPublicMarketplace(boolean v) {
        BYPASS.set(v);
    }

    public static boolean isBypassForPublicMarketplace() {
        return Boolean.TRUE.equals(BYPASS.get());
    }

    public static void clear() {
        CURRENT.remove();
        BYPASS.remove();
    }
}
