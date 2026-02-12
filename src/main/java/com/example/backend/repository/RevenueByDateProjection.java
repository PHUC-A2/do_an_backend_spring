package com.example.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RevenueByDateProjection {

    LocalDate getDate();

    BigDecimal getRevenue();
}
