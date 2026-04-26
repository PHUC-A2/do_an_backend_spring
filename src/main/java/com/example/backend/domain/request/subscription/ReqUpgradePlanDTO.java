package com.example.backend.domain.request.subscription;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpgradePlanDTO {
    @NotNull
    private Long tenantId;
    @NotNull
    private Long newPlanId;
}
