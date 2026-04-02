package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.backend.domain.entity.SupportIssueGuide;

public interface SupportIssueGuideRepository extends JpaRepository<SupportIssueGuide, Long> {

    @Query("select coalesce(max(g.sortOrder), -1) from SupportIssueGuide g")
    Integer findMaxSortOrder();
}
