package com.example.backend.domain.request.subscription;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqRenewSubscriptionDTO {
    @NotNull
    private Long subscriptionId;
}
