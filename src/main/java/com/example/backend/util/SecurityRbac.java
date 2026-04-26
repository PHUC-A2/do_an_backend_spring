package com.example.backend.util;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Trích quyền từ SecurityContext (cùng nguồn với {@code hasAuthority} trên controller).
 */
public final class SecurityRbac {

    private SecurityRbac() {
    }

    public static boolean hasAllAuthority() {
        return currentUserHasAuthority("ALL");
    }

    public static boolean currentUserHasAuthority(String authority) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) {
            return false;
        }
        for (GrantedAuthority ga : a.getAuthorities()) {
            if (authority.equals(ga.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    public static @Nullable String currentUsernameOrNull() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a == null || a.getName() == null ? null : a.getName();
    }
}
