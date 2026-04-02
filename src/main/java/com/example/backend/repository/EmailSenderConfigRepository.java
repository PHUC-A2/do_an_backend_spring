package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.EmailSenderConfig;

@Repository
public interface EmailSenderConfigRepository extends JpaRepository<EmailSenderConfig, Long> {
    Optional<EmailSenderConfig> findFirstByActiveTrueOrderByIdDesc();
}
