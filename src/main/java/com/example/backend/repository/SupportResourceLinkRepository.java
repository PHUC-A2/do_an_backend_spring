package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.backend.domain.entity.SupportResourceLink;

public interface SupportResourceLinkRepository extends JpaRepository<SupportResourceLink, Long> {

    @Query("select coalesce(max(l.sortOrder), -1) from SupportResourceLink l")
    Integer findMaxSortOrder();
}
