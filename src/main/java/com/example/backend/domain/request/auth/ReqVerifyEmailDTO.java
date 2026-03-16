package com.example.backend.domain.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqVerifyEmailDTO {

    @NotNull(message = "userId không được để trống")
    private Long userId;

    @NotBlank(message = "email không được để trống")
    private String email;

    @NotBlank(message = "otp không được để trống")
    private String otp;
}
