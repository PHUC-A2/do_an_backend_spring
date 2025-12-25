package com.example.backend.domain.response.role;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResUpdateRoleDTO {
    private Long id;
    private String name;
    private String description;
    private Instant updatedAt;
    private String updatedBy;
}
