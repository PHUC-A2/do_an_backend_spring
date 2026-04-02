package com.example.backend.domain.response.systemconfig;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResBankAccountConfigDTO {
    private Long id;
    private String bankCode;
    private String accountNoMasked;
    private String accountNameMasked;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
