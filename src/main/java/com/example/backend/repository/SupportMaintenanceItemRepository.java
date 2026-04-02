package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.backend.domain.entity.SupportMaintenanceItem;

public interface SupportMaintenanceItemRepository extends JpaRepository<SupportMaintenanceItem, Long> {

    @Query("select coalesce(max(m.sortOrder), -1) from SupportMaintenanceItem m")
    Integer findMaxSortOrder();
}
