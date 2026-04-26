package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.domain.entity.SecuritySettings;

public interface SecuritySettingsRepository extends JpaRepository<SecuritySettings, Long> {

    Optional<SecuritySettings> findByTenantId(Long tenantId);
}
