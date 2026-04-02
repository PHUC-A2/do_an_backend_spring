package com.example.backend.domain.response.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResSupportContactDTO {
    private Long id;
    private String name;
    private String role;
    private String phone;
    private String email;
    private String note;
    private Integer sortOrder;
}
