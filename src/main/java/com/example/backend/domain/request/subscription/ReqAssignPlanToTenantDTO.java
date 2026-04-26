package com.example.backend.domain.request.subscription;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqAssignPlanToTenantDTO {
    @NotNull
    private Long tenantId;
    @NotNull
    private Long planId;
    /** Mặc định: now */
    private Instant startDate;
}
