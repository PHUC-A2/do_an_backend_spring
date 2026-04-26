package com.example.backend.domain.entity;

import com.example.backend.domain.entity.base.BaseTenantEntity;
import com.example.backend.util.constant.equipment.EquipmentMobilityEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pitch_equipments", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "pitch_id", "equipment_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PitchEquipment extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pitch_id", nullable = false)
    private Pitch pitch;

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false)
    private Integer quantity;

    @Column(columnDefinition = "TEXT")
    private String specification; // Ví dụ: 7m x 2.5m, đèn LED 400W

    @Column(columnDefinition = "TEXT")
    private String note; // Ghi chú thêm cho hiển thị phía client

    /**
     * FIXED: thiết bị cố định / mô tả sân (không mượn qua booking).
     * MOVABLE: thiết bị cho mượn thêm khi đặt sân (có luồng mượn–trả).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_mobility", length = 16)
    private EquipmentMobilityEnum equipmentMobility = EquipmentMobilityEnum.FIXED;
}
