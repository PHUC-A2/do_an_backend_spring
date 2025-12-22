package com.example.backend.domain.response.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtUserDTO { // người dùng trong token
    private Long id;
    private String email;
    private String name;
}