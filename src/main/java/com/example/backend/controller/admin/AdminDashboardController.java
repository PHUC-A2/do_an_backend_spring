package com.example.backend.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.response.dashboard.ResAdminSystemOverviewDTO;
import com.example.backend.service.AdminDashboardService;
import com.example.backend.util.annotation.ApiMessage;

import lombok.RequiredArgsConstructor;

/**
 * API thống kê tổng quan hệ thống cho trang dashboard admin.
 */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/system-overview")
    @ApiMessage("Lấy thống kê tổng quan hệ thống")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('REVENUE_VIEW_DETAIL')")
    public ResponseEntity<ResAdminSystemOverviewDTO> getSystemOverview() {
        return ResponseEntity.ok(adminDashboardService.getSystemOverview());
    }
}
