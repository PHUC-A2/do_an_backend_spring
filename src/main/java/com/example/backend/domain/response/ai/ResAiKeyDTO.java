package com.example.backend.domain.response.ai;

import java.time.Instant;

import com.example.backend.util.constant.ai.AiProviderEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResAiKeyDTO {
    private Long id;
    private AiProviderEnum provider;
    private String label;
    private String apiKeyMasked; // chỉ hiện 8 ký tự đầu + ***
    private boolean active;
    private long usageCount;
    private Instant lastUsedAt;
    private Instant createdAt;
}
