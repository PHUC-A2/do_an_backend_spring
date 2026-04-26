package com.example.backend.domain.request.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqAdminSwitchTenantDTO {
    /** {@code null} → ngữ cảnh mặc định hệ thống (id = 1). */
    private Long tenantId;
}
