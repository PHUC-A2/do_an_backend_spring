package com.example.backend.domain.response.account;

import java.util.List;

import com.example.backend.domain.response.role.ResRoleNestedDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountUserDTO {
    private Long id;
    private String name;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;

    private List<ResRoleNestedDTO> roles;
}
