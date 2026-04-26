package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.domain.entity.AiChatSession;
import com.example.backend.domain.entity.User;

public interface AiChatSessionRepository extends JpaRepository<AiChatSession, Long> {

    long countByTenantId(long tenantId);

    Optional<AiChatSession> findByUserAndSessionDate(User user, String sessionDate);
    Optional<AiChatSession> findByUserIsNullAndSessionDate(String sessionDate);
}
