package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.SupportResourceLink;

public interface SupportResourceLinkRepository extends JpaRepository<SupportResourceLink, Long> {

    List<SupportResourceLink> findByTenantId(Long tenantId);

    @Query("select coalesce(max(l.sortOrder), -1) from SupportResourceLink l where l.tenantId = :tenantId")
    Integer findMaxSortOrder(@Param("tenantId") Long tenantId);
}
