package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.EmailSenderConfig;

@Repository
public interface EmailSenderConfigRepository extends JpaRepository<EmailSenderConfig, Long> {
    List<EmailSenderConfig> findByTenantIdOrderByIdDesc(Long tenantId);

    Optional<EmailSenderConfig> findFirstByActiveTrueAndTenantIdOrderByIdDesc(Long tenantId);

    Optional<EmailSenderConfig> findByIdAndTenantId(Long id, Long tenantId);

    Optional<EmailSenderConfig> findFirstByActiveTrueOrderByIdDesc();
}
