package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.BankAccountConfig;

@Repository
public interface BankAccountConfigRepository extends JpaRepository<BankAccountConfig, Long> {
    Optional<BankAccountConfig> findFirstByActiveTrueOrderByIdDesc();
}
