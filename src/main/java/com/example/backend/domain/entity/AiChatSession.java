package com.example.backend.domain.entity;

import java.time.Instant;

import com.example.backend.domain.entity.base.BaseTenantEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_chat_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AiChatSession extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // null = anonymous/guest
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Đếm tổng số lần chat trong ngày
    @Column(nullable = false)
    private int totalMessageCount = 0;

    // Số lần chat off-topic (câu hỏi không liên quan hệ thống)
    @Column(nullable = false)
    private int offTopicCount = 0;

    // Ngày reset count (mỗi ngày reset lại)
    @Column(nullable = false)
    private String sessionDate; // yyyy-MM-dd

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
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
