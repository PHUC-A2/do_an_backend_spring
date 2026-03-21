package com.example.backend.domain.response.equipment;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResEquipmentUsageStatsDTO {
    private List<ResEquipmentUsageRowDTO> byEquipment;
    private List<ResEquipmentUsageRowDTO> byPitch;
}
