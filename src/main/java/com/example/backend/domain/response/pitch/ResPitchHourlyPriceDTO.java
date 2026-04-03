package com.example.backend.domain.response.pitch;

import java.math.BigDecimal;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResPitchHourlyPriceDTO {

    // Thời gian bắt đầu của khung giá
    private LocalTime startTime;

    // Thời gian kết thúc của khung giá (end-exclusive)
    private LocalTime endTime;

    // Giá áp dụng theo giờ
    private BigDecimal pricePerHour;
}

