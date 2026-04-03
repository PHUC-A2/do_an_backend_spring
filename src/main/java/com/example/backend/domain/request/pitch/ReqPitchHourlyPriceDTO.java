package com.example.backend.domain.request.pitch;

import java.math.BigDecimal;
import java.time.LocalTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqPitchHourlyPriceDTO {

    // Thời gian bắt đầu của khung giờ (áp dụng theo ngày)
    @NotNull(message = "Giờ bắt đầu khung giá không được để trống")
    private LocalTime startTime;

    // Thời gian kết thúc của khung giờ (tính theo end-exclusive)
    @NotNull(message = "Giờ kết thúc khung giá không được để trống")
    private LocalTime endTime;

    // Giá theo giờ áp dụng trong khung thời gian trên
    @NotNull(message = "Giá theo giờ không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal pricePerHour;
}

