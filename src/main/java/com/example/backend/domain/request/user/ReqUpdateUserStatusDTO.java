package com.example.backend.domain.request.user;

import com.example.backend.util.constant.user.UserStatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqUpdateUserStatusDTO {

    @NotNull(message = "status không được để trống")
    private UserStatusEnum status;

    private String reason;
}
