package com.example.backend.domain.response.user;

import java.time.Instant;

import com.example.backend.util.constant.user.UserStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ResCreateUserDTO {

    private Long id;
    private String name;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private UserStatusEnum status;
    private Instant createdAt;
}
