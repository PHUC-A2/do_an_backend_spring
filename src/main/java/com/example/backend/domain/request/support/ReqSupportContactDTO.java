package com.example.backend.domain.request.support;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqSupportContactDTO {

    @NotBlank(message = "Họ tên không được để trống")
    private String name;

    @NotBlank(message = "Vai trò không được để trống")
    private String role;

    private String phone;
    private String email;
    private String note;

    private Integer sortOrder;
}
