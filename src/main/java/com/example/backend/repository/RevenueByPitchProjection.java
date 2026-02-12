package com.example.backend.repository;

import java.math.BigDecimal;

public interface RevenueByPitchProjection {
    Long getPitchId();

    String getPitchName();

    BigDecimal getRevenue();
}
