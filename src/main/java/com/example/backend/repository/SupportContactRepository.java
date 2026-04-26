package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.SupportContact;

public interface SupportContactRepository extends JpaRepository<SupportContact, Long> {

    List<SupportContact> findByTenantId(Long tenantId);

    @Query("select coalesce(max(c.sortOrder), -1) from SupportContact c where c.tenantId = :tenantId")
    Integer findMaxSortOrder(@Param("tenantId") Long tenantId);
}
