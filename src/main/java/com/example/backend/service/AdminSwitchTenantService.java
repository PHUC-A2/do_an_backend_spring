package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.response.login.JwtUserDTO;
import com.example.backend.domain.response.login.LoginUserDTO;
import com.example.backend.domain.response.login.ResLoginDTO;
import com.example.backend.repository.TenantRepository;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.error.BadRequestException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminSwitchTenantService {

    private final TenantRepository tenantRepository;
    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final SecurityUtil securityUtil;

    @Getter
    public static final class AdminSwitchTokenResult {
        private final ResLoginDTO resLogin;
        private final String refreshToken;

        AdminSwitchTokenResult(ResLoginDTO resLogin, String refreshToken) {
            this.resLogin = resLogin;
            this.refreshToken = refreshToken;
        }
    }

    @Transactional
    @NonNull
    public AdminSwitchTokenResult issueImpersonationTokens(@NonNull User user, Long tenantIdOrNull) {
        long target = tenantIdOrNull == null || tenantIdOrNull == TenantService.DEFAULT_TENANT_ID
                ? TenantService.DEFAULT_TENANT_ID
                : tenantIdOrNull;
        if (target != TenantService.DEFAULT_TENANT_ID) {
            if (!tenantRepository.existsById(target)) {
                throw new BadRequestException("Tenant không tồn tại");
            }
        }
        List<String> authorities = subscriptionService.resolveAuthorityNamesForToken(user, target);
        JwtUserDTO jwtUser = new JwtUserDTO();
        jwtUser.setId(user.getId());
        jwtUser.setEmail(user.getEmail());
        jwtUser.setName(user.getName());
        jwtUser.setTenantId(target);
        jwtUser.setPlan(subscriptionService.resolvePlanNameForToken(user, target));
        jwtUser.setPermissions(new ArrayList<>(authorities));

        String accessToken = securityUtil.createAccessToken(user.getEmail(), jwtUser, authorities);
        String refreshToken = securityUtil.createRefreshToken(user.getEmail(), jwtUser);
        userService.updateUserToken(refreshToken, user.getEmail());

        ResLoginDTO res = new ResLoginDTO();
        res.setAccessToken(accessToken);
        res.setUser(
                new LoginUserDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail()));
        res.setCurrentTenantId(target);
        return new AdminSwitchTokenResult(res, refreshToken);
    }
}
