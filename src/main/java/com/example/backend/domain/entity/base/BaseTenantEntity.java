package com.example.backend.domain.entity.base;

import com.example.backend.tenant.TenantContext;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseTenantEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @PrePersist
    public void prePersistBaseTenant() {
        if (tenantId == null) {
            tenantId = TenantContext.getCurrentTenantId().orElse(1L);
        }
    }
}
