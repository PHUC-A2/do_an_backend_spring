package com.example.backend.domain.response.pitch;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;

import com.example.backend.util.constant.pitch.PitchStatusEnum;
import com.example.backend.util.constant.pitch.PitchTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResPitchDTO {
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
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
