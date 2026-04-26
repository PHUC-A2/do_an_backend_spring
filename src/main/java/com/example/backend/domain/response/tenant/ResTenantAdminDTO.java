package com.example.backend.domain.response.tenant;

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
public class ResTenantAdminDTO {
    private Long id;
    private String slug;
    private String name;
    private String status;
    private String contactPhone;
    private String contactEmail;
    private String description;
    private Long ownerUserId;
    private String ownerEmail;
    private String ownerName;
}
