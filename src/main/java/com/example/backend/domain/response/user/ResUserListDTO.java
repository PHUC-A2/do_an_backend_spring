package com.example.backend.domain.response.user;

import java.time.Instant;
import java.util.List;

import com.example.backend.domain.response.role.ResRoleNestedDTO;
import com.example.backend.util.constant.user.UserStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResUserListDTO {
    
    private Long id;
    private String name;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private UserStatusEnum status;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    private List<ResRoleNestedDTO> roles;
}
