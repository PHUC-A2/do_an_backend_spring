package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.SupportMaintenanceItem;

public interface SupportMaintenanceItemRepository extends JpaRepository<SupportMaintenanceItem, Long> {

    List<SupportMaintenanceItem> findByTenantId(Long tenantId);

    @Query("select coalesce(max(m.sortOrder), -1) from SupportMaintenanceItem m where m.tenantId = :tenantId")
    Integer findMaxSortOrder(@Param("tenantId") Long tenantId);
}
