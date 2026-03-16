package com.example.backend.domain.response.user;

import java.time.Instant;

import com.example.backend.util.constant.user.UserStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResUpdateUserStatusDTO {
    private Long id;
    private UserStatusEnum status;
    private String bannedReason;
    private Instant bannedAt;
    private Instant updatedAt;
}
