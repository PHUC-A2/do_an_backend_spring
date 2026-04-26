package com.example.backend.domain.request.tenant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateTenantAdminDTO {

    @NotBlank(message = "Tên tenant không được để trống")
    private String name;

    /** Nếu để trống, hệ thống tạo từ tên. */
    private String slug;

    private String contactPhone;
    private String contactEmail;
    private String description;

    /** PENDING | APPROVED | REJECTED (mặc định PENDING) */
    private String status;

    @NotNull(message = "Cần chọn tài khoản chủ sân (owner user id)")
    private Long ownerUserId;
}
