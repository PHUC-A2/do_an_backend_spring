package com.example.backend.domain.response.pitchequipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResPitchEquipmentDTO {

    private Long id;
    private Long pitchId;
    private Long equipmentId;
    private String equipmentName;
    private String equipmentImageUrl;
    private Integer quantity;
    private String specification;
    private String note;
}
