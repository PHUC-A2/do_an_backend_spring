package com.example.backend.domain.entity;

import java.time.Instant;

import com.example.backend.util.SecurityUtil;
import com.example.backend.util.constant.equipment.EquipmentBorrowLogTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "equipment_borrow_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EquipmentBorrowLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "booking_equipment_id", nullable = false)
    private BookingEquipment bookingEquipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EquipmentBorrowLogTypeEnum logType;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Người thực hiện thao tác (snapshot): mượn = thường là người đặt; trả = có thể là người khác. */
    @Column(length = 200)
    private String actorName;

    @Column(length = 32)
    private String actorPhone;

    private Instant createdAt;

    private String createdBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdAt = Instant.now();
    }
}
