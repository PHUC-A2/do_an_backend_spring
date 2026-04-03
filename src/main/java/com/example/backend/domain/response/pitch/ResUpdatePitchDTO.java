package com.example.backend.domain.response.pitch;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import com.example.backend.util.constant.pitch.PitchStatusEnum;
import com.example.backend.util.constant.pitch.PitchTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResUpdatePitchDTO {
    private Long id;
    private String name;
    private PitchTypeEnum pitchType;;
    private BigDecimal pricePerHour;
    private String pitchUrl;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean open24h;
    private PitchStatusEnum status;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double length;
    private Double width;
    private Double height;
    private String imageUrl;
    // Danh sách khung giờ áp dụng giá khác nhau theo ngày (nếu có)
    private List<ResPitchHourlyPriceDTO> hourlyPrices;
    private Double averageRating;
    private Long reviewCount;
    private Instant updatedAt;
    private String updatedBy;
}
