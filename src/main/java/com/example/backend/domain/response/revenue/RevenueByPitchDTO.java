package com.example.backend.domain.response.revenue;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RevenueByPitchDTO {
    private Long pitchId;
    private String pitchName;
    private BigDecimal revenue;
}
