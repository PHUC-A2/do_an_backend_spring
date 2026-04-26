package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.BankAccountConfig;

@Repository
public interface BankAccountConfigRepository extends JpaRepository<BankAccountConfig, Long> {
    List<BankAccountConfig> findByTenantIdOrderByIdDesc(Long tenantId);

    Optional<BankAccountConfig> findFirstByActiveTrueAndTenantIdOrderByIdDesc(Long tenantId);

    Optional<BankAccountConfig> findByIdAndTenantId(Long id, Long tenantId);

    Optional<BankAccountConfig> findFirstByActiveTrueOrderByIdDesc();
}
