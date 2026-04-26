package com.example.backend.domain.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqRegisterDTO {

    private String name;

    /** Tên cửa hàng / sân (tùy chọn): nếu có sẽ tạo tenant mới gắn tài khoản làm OWNER. */
    private String shopName;

    @NotBlank(message = "email không được để trống")
    private String email;

    @NotBlank(message = "password không được để trống")
    private String password;
}
