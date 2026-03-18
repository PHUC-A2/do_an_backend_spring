package com.example.backend.domain.entity;

import java.time.Instant;

import com.example.backend.util.constant.ai.AiProviderEnum;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_api_keys")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AiApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiProviderEnum provider;

    @Column(nullable = false, length = 1000)
    private String apiKey;

    @Column(length = 200)
    private String label; // e.g. "Key #1", "Backup key"

    @Column(nullable = false)
    private boolean active = true;

    // Để theo dõi lượt dùng
    @Column(nullable = false)
    private long usageCount = 0;

    @Column
    private Instant lastUsedAt;

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
