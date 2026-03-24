package com.example.backend.domain.response.review;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResReviewMessageDTO {
    private Long id;
    private Long reviewId;
    private Long senderId;
    private String senderName;
    private String senderFullName;
    private String senderAvatarUrl;
    private String content;
    private Instant deliveredAt;
    private Instant readAt;
    private Instant createdAt;
}
