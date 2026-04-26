package com.example.backend.domain.response.subscription;

import java.time.Instant;

import com.example.backend.util.constant.subscription.SubscriptionStatusEnum;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResSubscriptionListDTO {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private Long planId;
    private String planName;
    private Instant startDate;
    private Instant endDate;
    private SubscriptionStatusEnum status;
}
