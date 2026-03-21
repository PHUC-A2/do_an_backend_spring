package com.example.backend.domain.response.equipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResEquipmentUsageRowDTO {
    private Long id;
    private String name;
    private Long borrowCount;
}
