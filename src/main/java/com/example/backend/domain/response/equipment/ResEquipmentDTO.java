package com.example.backend.domain.response.equipment;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.backend.util.constant.equipment.EquipmentStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResEquipmentDTO {
    private Long id;
    private String name;
    private String description;
    private Integer totalQuantity;
    private Integer availableQuantity;
    /** Tổng SL đã gắn trên mọi sân (pitch_equipments). */
    private Integer quantityAllocatedOnPitches;
    /** SL còn chưa gắn sân = totalQuantity - quantityAllocatedOnPitches (hiển thị “kho” khi cấu hình sân). */
    private Integer quantityUnassignedToPitches;
    private BigDecimal price;
    private String imageUrl;
    private EquipmentStatusEnum status;
    private String conditionNote;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
