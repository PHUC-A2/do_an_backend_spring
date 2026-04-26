package com.example.backend.domain.response.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResRoleNestedDTO {
    private Long id;
    /** null: role toàn hệ thống; non-null: thuộc shop. */
    private Long tenantId;
    private String name;
    private String description;
}
