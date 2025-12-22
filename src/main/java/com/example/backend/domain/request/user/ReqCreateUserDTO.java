package com.example.backend.domain.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

// request chỉ chứa các trường có NotBlank
public class ReqCreateUserDTO {

    private String name;
    private String fullName;

    @NotBlank(message = "email không được để trống")
    private String email;

    @NotBlank(message = "password không được để trống")
    private String password;

    private String phoneNumber;

    private String avatarUrl;

}
