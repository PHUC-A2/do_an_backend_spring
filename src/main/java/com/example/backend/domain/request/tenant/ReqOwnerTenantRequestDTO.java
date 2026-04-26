package com.example.backend.domain.request.tenant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqOwnerTenantRequestDTO {

    @NotBlank(message = "Tên sân / thương hiệu không được để trống")
    private String shopName;

    private String contactPhone;

    @NotBlank(message = "Vui lòng nhập email đăng ký chủ sân")
    @Email(message = "Email không hợp lệ")
    private String contactEmail;

    private String description;
}
