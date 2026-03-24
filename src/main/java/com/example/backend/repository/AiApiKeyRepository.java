package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.entity.AiApiKey;
import com.example.backend.util.constant.ai.AiProviderEnum;

@Repository
public interface AiApiKeyRepository extends JpaRepository<AiApiKey, Long> {

    List<AiApiKey> findByProviderOrderByIdAsc(AiProviderEnum provider);

    List<AiApiKey> findByProviderAndActiveTrueOrderByIdAsc(AiProviderEnum provider);

    Optional<AiApiKey> findFirstByProviderAndActiveTrue(AiProviderEnum provider);

    long countByProviderAndActiveTrue(AiProviderEnum provider);

    long countByActiveTrue();

    /** Tìm bản ghi key theo provider + chuỗi key (dùng khi đánh dấu lỗi, kể cả sau khi đã tắt). */
    Optional<AiApiKey> findFirstByProviderAndApiKeyOrderByIdAsc(AiProviderEnum provider, String apiKey);
}
