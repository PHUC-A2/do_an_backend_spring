package com.example.backend.domain.request.security;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateSecuritySettingsDTO {

    @NotNull(message = "paymentConfirmationPinRequired không được null")
    private Boolean paymentConfirmationPinRequired;
}
