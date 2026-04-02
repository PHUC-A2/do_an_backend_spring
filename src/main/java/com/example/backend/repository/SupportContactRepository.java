package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.backend.domain.entity.SupportContact;

public interface SupportContactRepository extends JpaRepository<SupportContact, Long> {

    @Query("select coalesce(max(c.sortOrder), -1) from SupportContact c")
    Integer findMaxSortOrder();
}
