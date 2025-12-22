package com.example.backend.domain.response.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountUserDTO {
    private Long id;
    private String email;
    private String name;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
}
