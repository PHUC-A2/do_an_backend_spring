package com.example.backend.domain.request.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqResetPaymentPinWithOtpDTO {

    @NotBlank(message = "OTP không được để trống")
    private String otp;

    @NotBlank(message = "PIN mới không được để trống")
    @Pattern(regexp = "^\\d{6}$", message = "PIN mới phải gồm đúng 6 chữ số")
    private String newPin;

    @NotBlank(message = "Nhập lại PIN không được để trống")
    @Pattern(regexp = "^\\d{6}$", message = "Nhập lại PIN phải gồm đúng 6 chữ số")
    private String confirmPin;
}
