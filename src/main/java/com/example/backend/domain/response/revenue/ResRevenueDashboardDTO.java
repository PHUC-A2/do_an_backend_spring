package com.example.backend.domain.response.revenue;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResRevenueDashboardDTO {

    // ===== 1. Tổng quan =====
    private BigDecimal totalRevenue; // Tổng doanh thu
    private BigDecimal todayRevenue; // Doanh thu hôm nay
    private BigDecimal weekRevenue; // Doanh thu theo tuần
    private BigDecimal monthRevenue; // Doanh thu tháng

    private Long totalBookings; // Tổng booking
    private Long paidBookings; // Booking đã thanh toán
    private Long cancelledBookings; // Booking đã hủy

    private Long totalUsers;
    private Long totalPitches;

    // ===== 2. Biểu đồ theo ngày =====
    private List<RevenuePointDTO> revenueByDate;

    // ===== 3. Doanh thu theo sân =====
    private List<RevenueByPitchDTO> revenueByPitch;
}
