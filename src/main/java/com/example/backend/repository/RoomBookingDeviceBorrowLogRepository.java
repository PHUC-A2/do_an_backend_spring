package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.backend.domain.entity.RoomBookingDeviceBorrowLog;

/**
 * Repository nhật ký mượn/trả theo dòng thiết bị của rooms.
 *
 * <p>
 * Clone theo {@code EquipmentBorrowLogRepository}.
 * </p>
 */
public interface RoomBookingDeviceBorrowLogRepository extends JpaRepository<RoomBookingDeviceBorrowLog, Long> {

    /** Lấy log gần nhất (Top 200 theo createdAt desc) để hiển thị tab "Nhật ký mượn/trả". */
    @Query(value = "SELECT l FROM RoomBookingDeviceBorrowLog l ORDER BY l.createdAt DESC")
    List<RoomBookingDeviceBorrowLog> findTop200ByOrderByCreatedAtDesc();
}

