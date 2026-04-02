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
public class ResSupportMaintenanceItemDTO {
    private Long id;
    private String label;
    private String frequencyText;
    private String color;
    private Integer sortOrder;
}
