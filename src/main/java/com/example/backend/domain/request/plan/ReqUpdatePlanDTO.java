package com.example.backend.domain.request.plan;

import java.math.BigDecimal;

import com.example.backend.util.constant.subscription.PlanStatusEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdatePlanDTO {
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private BigDecimal price;
    @Positive
    @NotNull
    private Integer durationDays;
    @NotNull
    private PlanStatusEnum status;
}
