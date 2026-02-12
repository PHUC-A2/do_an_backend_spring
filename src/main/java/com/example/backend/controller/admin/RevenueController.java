package com.example.backend.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.response.revenue.ResRevenueDashboardDTO;
import com.example.backend.service.RevenueService;
import com.example.backend.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class RevenueController {

    private final RevenueService revenueService;

    public RevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @GetMapping("/revenues")
    @ApiMessage("Lấy thống kê doanh thu")
    public ResponseEntity<ResRevenueDashboardDTO> getDashboard() {
        return ResponseEntity.ok(this.revenueService.getDashboard());
    }

}
