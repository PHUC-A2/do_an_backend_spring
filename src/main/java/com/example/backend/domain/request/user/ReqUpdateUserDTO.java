package com.example.backend.domain.request.user;

import com.example.backend.util.constant.user.UserStatusEnum;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqUpdateUserDTO {


    @NotBlank(message = "name không được để trống")
    private String name;

    @NotBlank(message = "fullName không được để trống")
    private String fullName;

    @NotBlank(message = "phoneNumber không được để trống")
    private String phoneNumber;

    private String avatarUrl;

    private UserStatusEnum status;
}
