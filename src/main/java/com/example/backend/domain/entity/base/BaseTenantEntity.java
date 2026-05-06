package com.example.backend.domain.entity.base;

import com.example.backend.tenant.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Getter
@Setter
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class BaseTenantEntity {

    @Column(name = "tenant_id", nullable = false, insertable = false, updatable = false)
    private Long tenantId;

    @PrePersist
    public void prePersistBaseTenant() {
        if (tenantId == null) {
            tenantId = TenantContext.requireCurrentTenantId();
        }
    }
}
