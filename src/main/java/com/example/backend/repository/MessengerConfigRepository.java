package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.MessengerConfig;

@Repository
public interface MessengerConfigRepository extends JpaRepository<MessengerConfig, Long> {
    List<MessengerConfig> findByTenantIdOrderByIdDesc(Long tenantId);

    Optional<MessengerConfig> findFirstByActiveTrueAndTenantIdOrderByIdDesc(Long tenantId);

    Optional<MessengerConfig> findByIdAndTenantId(Long id, Long tenantId);

    Optional<MessengerConfig> findFirstByActiveTrueOrderByIdDesc();
}
