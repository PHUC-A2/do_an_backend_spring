package com.example.backend.domain.response.role;

import java.util.List;

import com.example.backend.domain.response.permission.ResPermissionNestedDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResRoleNestedDTO {
    private Long id;
    private String name;
    private String description;

    private List<ResPermissionNestedDTO> permissions;
}
