package com.example.backend.domain.response.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResTenantDTO {
    private Long id;
    private String slug;
    private String name;
    /** PENDING / APPROVED / REJECTED */
    private String status;
}
