package com.example.backend.domain.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqResendOtpByEmailDTO {

    @NotBlank(message = "email không được để trống")
    private String email;
}
