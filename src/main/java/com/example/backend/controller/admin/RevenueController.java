package com.example.backend.controller.admin;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.domain.response.revenue.ResRevenueDashboardDTO;
import com.example.backend.service.RevenueService;
import com.example.backend.util.annotation.ApiMessage;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RevenueController {

    private final RevenueService revenueService;

    @GetMapping("/revenues")
    @ApiMessage("Lấy thống kê doanh thu")
    public ResponseEntity<ResRevenueDashboardDTO> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (from == null) {
            from = LocalDate.now().withDayOfMonth(1); // đầu tháng
        }

        if (to == null) {
            to = LocalDate.now();
        }

        return ResponseEntity.ok(
                revenueService.getDashboard(from, to));
    }

}
