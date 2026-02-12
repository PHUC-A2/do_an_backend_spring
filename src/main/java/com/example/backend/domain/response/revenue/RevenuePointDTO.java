package com.example.backend.domain.response.revenue;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RevenuePointDTO {
    private String label; // 2026-02-10
    private BigDecimal revenue; // tiền ngày đó
}
