package com.example.backend.domain.request.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqSwitchTenantDTO {
    @NotNull
    private Long tenantId;
}
