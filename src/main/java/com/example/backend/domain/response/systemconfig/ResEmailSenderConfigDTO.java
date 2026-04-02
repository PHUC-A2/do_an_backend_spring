package com.example.backend.domain.response.systemconfig;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResEmailSenderConfigDTO {
    private Long id;
    private String email;
    private String passwordMasked;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
