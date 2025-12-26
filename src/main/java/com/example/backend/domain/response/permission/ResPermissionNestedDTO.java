package com.example.backend.domain.response.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResPermissionNestedDTO {
    private Long id;
    private String name;
    private String description;
}
