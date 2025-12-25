package com.example.backend.domain.response.permission;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResUpdatePermissionDTO {
    private Long id;
    private String name;
    private String description;
    private Instant updatedAt;
    private String updatedBy;
}
