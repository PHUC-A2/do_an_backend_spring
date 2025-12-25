package com.example.backend.domain.response.permission;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResCreatePermissionDTO {
    private Long id;
    private String name;
    private String description;
    private Instant createdAt;
    private String createdBy;
}
