package com.example.backend.domain.entity;

import com.example.backend.util.constant.booking.BookingEquipmentStatusEnum;

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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "booking_equipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BookingEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false)
    private Integer quantity; // số lượng mượn

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingEquipmentStatusEnum status = BookingEquipmentStatusEnum.BORROWED;

    // Tiền phạt khi mất thiết bị (LOST = quantity × equipment.price)
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long penaltyAmount = 0L;

    // Soft-delete phía client: true = ẩn khỏi danh sách của client, admin vẫn thấy
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deletedByClient = false;
}
