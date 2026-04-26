package com.example.backend.domain.response.plan;

import java.math.BigDecimal;

import com.example.backend.util.constant.subscription.PlanStatusEnum;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResPlanDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int durationDays;
    private PlanStatusEnum status;
}
