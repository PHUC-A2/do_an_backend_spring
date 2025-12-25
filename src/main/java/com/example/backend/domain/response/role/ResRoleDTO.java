package com.example.backend.domain.response.role;

import java.time.Instant;
import java.util.List;

import com.example.backend.domain.response.permission.ResPermissionDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResRoleDTO {
    private Long id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    private List<ResPermissionDTO> permissions;
}
