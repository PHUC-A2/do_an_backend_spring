package com.example.backend.domain.request.auth.resetpw;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqResetPasswordDTO {
    private String email;
    private String otp;
    private String newPassword;
}
