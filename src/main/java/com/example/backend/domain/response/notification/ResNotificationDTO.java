package com.example.backend.domain.response.notification;

import java.time.Instant;

import com.example.backend.util.constant.notification.NotificationTypeEnum;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResNotificationDTO {
    private Long id;
    private NotificationTypeEnum type;
    private String message;
    private Long senderId;
    private String senderName;
    private String senderAvatarUrl;
    private Long referenceId;
    @JsonProperty("isRead")
    private boolean isRead;
    @JsonProperty("deletedByUser")
    private boolean deletedByUser;
    private Instant createdAt;
}
