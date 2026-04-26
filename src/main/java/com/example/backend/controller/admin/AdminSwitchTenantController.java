package com.example.backend.controller.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.admin.ReqAdminSwitchTenantDTO;
import com.example.backend.domain.response.login.ResLoginDTO;
import com.example.backend.service.AdminSwitchTenantService;
import com.example.backend.service.AdminSwitchTenantService.AdminSwitchTokenResult;
import com.example.backend.service.UserService;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.BadRequestException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminSwitchTenantController {

    private final AdminSwitchTenantService adminSwitchTenantService;
    private final UserService userService;

    @Value("${backend.jwt.refresh-token-validity-in-second}")
    private long refreshTokenExpiration;

    @PostMapping("/switch-tenant")
    @PreAuthorize("hasAuthority('ALL')")
    @ApiMessage("Chuyển ngữ cảnh tenant (quản trị hệ thống)")
    public ResponseEntity<ResLoginDTO> switchTenant(@RequestBody(required = false) ReqAdminSwitchTenantDTO dto) {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Chưa đăng nhập"));
        User user = userService.handleGetUserByUsername(email);
        Long tid = dto == null ? null : dto.getTenantId();
        AdminSwitchTokenResult out = adminSwitchTenantService.issueImpersonationTokens(user, tid);
        ResLoginDTO body = out.getResLogin();

        ResponseCookie resCookies = ResponseCookie
                .from("refresh_token", out.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(body);
    }
}
