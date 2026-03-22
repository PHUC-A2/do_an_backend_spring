package com.example.backend.domain.entity.v2;

import java.time.Instant;
import java.time.LocalTime;

import com.example.backend.util.SecurityUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cấu hình lịch tiết cho phòng; ánh xạ bảng {@code room_schedules_v2}.
 */
@Entity
@Table(name = "room_schedules_v2")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoomSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "total_slots", nullable = false)
    private Integer totalSlots;

    @Column(name = "slot_duration", nullable = false)
    private Integer slotDuration;

    @Column(name = "break_duration", nullable = false)
    private Integer breakDuration;

    /**
     * JSON mảng phút nghỉ giữa các tiết buổi sáng (sau tiết 1, 2, …). Null = dùng {@code breakDuration} cho mọi khoảng.
     */
    @Column(name = "morning_gap_breaks_json", columnDefinition = "TEXT")
    private String morningGapBreaksJson;

    /**
     * JSON mảng phút nghỉ giữa các tiết buổi chiều. Null = dùng {@code breakDuration} đồng đều.
     */
    @Column(name = "afternoon_gap_breaks_json", columnDefinition = "TEXT")
    private String afternoonGapBreaksJson;

    @Column(name = "morning_start", nullable = false)
    private LocalTime morningStart;

    @Column(name = "morning_end", nullable = false)
    private LocalTime morningEnd;

    @Column(name = "afternoon_start", nullable = false)
    private LocalTime afternoonStart;

    @Column(name = "afternoon_end", nullable = false)
    private LocalTime afternoonEnd;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.updatedAt = Instant.now();
    }
}
