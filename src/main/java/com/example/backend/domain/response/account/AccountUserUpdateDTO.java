package com.example.backend.domain.response.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountUserUpdateDTO {
    private Long id;
    private String name;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
}
