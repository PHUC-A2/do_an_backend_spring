package com.example.backend.domain.response.equipment;

import com.example.backend.util.constant.equipment.EquipmentMobilityEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Một dòng gắn thiết bị trên sân — dùng khi xem chi tiết thiết bị (danh sách sân đang dùng loại thiết bị này).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResEquipmentPitchAssignmentDTO {

    private Long pitchEquipmentId;
    private Long pitchId;
    private String pitchName;
    private Integer quantity;
    private EquipmentMobilityEnum equipmentMobility;
    private String specification;
    private String note;
}
