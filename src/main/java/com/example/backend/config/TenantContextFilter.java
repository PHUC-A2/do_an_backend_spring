package com.example.backend.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.backend.domain.entity.Tenant;
import com.example.backend.repository.TenantRepository;
import com.example.backend.service.SubscriptionService;
import com.example.backend.service.TenantService;
import com.example.backend.tenant.TenantContext;
import com.example.backend.util.constant.tenant.TenantStatusEnum;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {

    private final TenantService tenantService;
    private final TenantRepository tenantRepository;
    private final SubscriptionService subscriptionService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String path = request.getRequestURI();
            if (isPublicMarketplaceRead(path)) {
                TenantContext.setBypassForPublicMarketplace(true);
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
                filterChain.doFilter(request, response);
                return;
            }

            Jwt jwt = jwtAuth.getToken();
            Long userId = extractUserId(jwt);
            Long jwtTenantId = extractTenantId(jwt);

            if (userId == null) {
                filterChain.doFilter(request, response);
                return;
            }

            String headerTid = request.getHeader("X-Tenant-Id");
            long effective = tenantService.resolveEffectiveTenantId(userId, jwtTenantId, headerTid);
            TenantContext.setCurrentTenantId(effective);

            if (requiresApprovedShopContext(request) && !hasAllAuthority(jwtAuth)) {
                Tenant t = tenantRepository.findById(effective).orElse(null);
                if (t == null || t.getStatus() != TenantStatusEnum.APPROVED) {
                    String msg = "{\"statusCode\":403,\"message\":\"Cửa hàng (tenant) chưa được duyệt hoặc đã bị từ chối. Chỉ xem marketplace cho đến khi quản trị hệ thống duyệt.\"}";
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.getOutputStream().write(msg.getBytes(StandardCharsets.UTF_8));
                    return;
                }
            }

            if (requiresApprovedShopContext(request) && !hasAllAuthority(jwtAuth)
                    && effective != SubscriptionService.DEFAULT_TENANT_ID
                    && subscriptionService.isExpired(effective)) {
                String msg = "{\"statusCode\":403,\"message\":\"Gói dịch vụ đã hết hạn. Vui lòng gia hạn.\"}";
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.getOutputStream().write(msg.getBytes(StandardCharsets.UTF_8));
                return;
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private static boolean hasAllAuthority(JwtAuthenticationToken auth) {
        return auth.getAuthorities().stream().anyMatch(a -> "ALL".equals(a.getAuthority()));
    }

    /**
     * Các API quản lý cần tenant đã APPROVED (trừ quản trị hệ thống có quyền ALL).
     * Cho phép marketplace: GET sân công khai, client, auth, duyệt tenant, thiết bị push, gửi yêu cầu chủ sân.
     */
    private static boolean requiresApprovedShopContext(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (!path.startsWith("/api/v1/")) {
            return false;
        }
        if (path.startsWith("/api/v1/client/") || path.startsWith("/api/v1/auth/")
                || path.startsWith("/api/v1/admin/tenants") || path.startsWith("/api/v1/tenant/")
                || path.startsWith("/api/v1/devices/")) {
            return false;
        }
        if ("GET".equals(method) && ("/api/v1/pitches".equals(path) || path.matches("/api/v1/pitches/\\d+"))) {
            return false;
        }
        if (path.startsWith("/api/v1/admin/")) {
            return true;
        }
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            return true;
        }
        return true;
    }

    private boolean isPublicMarketplaceRead(String path) {
        if (path == null) {
            return false;
        }
        if ("/api/v1/pitches".equals(path)) {
            return true;
        }
        if (path.startsWith("/api/v1/client/public/")) {
            return true;
        }
        return false;
    }

    private Long extractUserId(Jwt jwt) {
        Object u = jwt.getClaim("user");
        if (u instanceof Map<?, ?> m) {
            Object id = m.get("id");
            if (id instanceof Number n) {
                return n.longValue();
            }
        }
        return null;
    }

    private Long extractTenantId(Jwt jwt) {
        Object u = jwt.getClaim("user");
        if (u instanceof Map<?, ?> m) {
            Object tid = m.get("tenantId");
            if (tid instanceof Number n) {
                return n.longValue();
            }
        }
        return null;
    }
}
