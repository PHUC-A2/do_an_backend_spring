package com.example.backend.domain.request.systemconfig;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqEmailSenderConfigDTO {

    @NotBlank(message = "Email gửi không được để trống")
    @Email(message = "Email gửi không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu ứng dụng không được để trống")
    private String password;

    private Boolean active;
}
