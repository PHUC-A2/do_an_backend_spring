package com.example.backend.domain.response.role;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResRoleListDTO {
    private Long id;
    /** null = role toàn hệ thống; khác null = thuộc shop đó. */
    private Long tenantId;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

}
