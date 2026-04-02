package com.example.backend.domain.request.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqSetPaymentPinDTO {

    @NotBlank(message = "PIN không được để trống")
    @Pattern(regexp = "^\\d{6}$", message = "PIN phải gồm đúng 6 chữ số")
    private String pin;

    /** Bắt buộc khi đổi PIN đã tồn tại trước đó. */
    private String currentPin;
}
