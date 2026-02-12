package com.example.backend.domain.request.auth.resetpw;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqForgotPasswordDTO {
    private String email;
}
