package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.SupportIssueGuide;

public interface SupportIssueGuideRepository extends JpaRepository<SupportIssueGuide, Long> {

    List<SupportIssueGuide> findAllByTenantId(Long tenantId);

    @Query("select coalesce(max(g.sortOrder), -1) from SupportIssueGuide g where g.tenantId = :tenantId")
    Integer findMaxSortOrderForTenantId(@Param("tenantId") Long tenantId);
}
