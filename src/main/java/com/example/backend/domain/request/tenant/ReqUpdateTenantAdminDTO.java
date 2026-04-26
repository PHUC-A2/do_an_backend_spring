package com.example.backend.domain.request.tenant;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateTenantAdminDTO {

    @NotBlank(message = "Tên tenant không được để trống")
    private String name;

    @NotBlank(message = "Slug không được để trống")
    private String slug;

    private String contactPhone;
    private String contactEmail;
    private String description;

    /** PENDING | APPROVED | REJECTED */
    @NotBlank(message = "Trạng thái không được để trống")
    private String status;
}
