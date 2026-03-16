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
public class ReqResendOtpDTO {

    @NotBlank(message = "email không được để trống")
    private String email;

    @NotNull(message = "userId không được để trống")
    private Long userId;
}
