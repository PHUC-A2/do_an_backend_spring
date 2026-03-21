package com.example.backend.domain.response.equipment;

import java.time.Instant;

import com.example.backend.util.constant.equipment.EquipmentBorrowLogTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResEquipmentBorrowLogDTO {
    private Long id;
    private Long bookingEquipmentId;
    private Long bookingId;
    private Long equipmentId;
    private String equipmentName;
    private EquipmentBorrowLogTypeEnum logType;
    private String notes;
    private Instant createdAt;
    private String createdBy;
}
