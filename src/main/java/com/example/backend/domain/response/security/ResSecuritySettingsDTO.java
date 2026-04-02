package com.example.backend.domain.response.security;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResSecuritySettingsDTO {

    private boolean paymentConfirmationPinRequired;
}
